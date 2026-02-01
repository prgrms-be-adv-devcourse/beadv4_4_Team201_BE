package app.giftify.payment.adapter.inbound.web;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.payment.adapter.inbound.web.dto.PaymentChargeRequest;
import app.giftify.payment.adapter.inbound.web.dto.PaymentChargeResponse;
import app.giftify.payment.adapter.inbound.web.dto.PaymentConfirmRequest;
import app.giftify.payment.adapter.inbound.web.dto.PaymentConfirmResponse;
import app.giftify.payment.adapter.outbound.pg.TossConfirmResult;
import app.giftify.payment.application.inbound.ConfirmPaymentCommand;
import app.giftify.payment.application.inbound.ConfirmPaymentUseCase;
import app.giftify.payment.application.inbound.CreatePaymentCommand;
import app.giftify.payment.application.inbound.CreatePaymentUseCase;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.application.inbound.QueryPaymentUseCase;
import app.giftify.payment.application.outbound.PaymentGateway;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentMethod;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.CommonResponse;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v2/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentV2Api {
	private final CreatePaymentUseCase createPaymentUseCase;
	private final ConfirmPaymentUseCase confirmPaymentUseCase;
	private final QueryPaymentUseCase queryPaymentUseCase;
	private final PaymentGateway paymentGateway;
	private final PaymentRepository paymentRepository;  // 추가

	@Override
	@PostMapping("/charge")
	public ResponseEntity<CommonResponse<PaymentChargeResponse>> charge(
		@CurrentMemberId Long memberId,
		@Valid @RequestBody PaymentChargeRequest request
	) {
		log.info("[PaymentController] 결제 생성 요청. memberId={}, amount={}", memberId, request.amount());

		String orderId = request.orderId() != null ? request.orderId() : "CHG-" + UUID.randomUUID();
		String idempotencyKey = UUID.randomUUID().toString();
		var requestedAmount = Money.of(request.amount());

		CreatePaymentCommand command = new CreatePaymentCommand(
			idempotencyKey,
			memberId,
			orderId,
			PaymentType.POINT_CHARGE,
			PaymentMethod.CARD,
			requestedAmount,
			Collections.emptyList()
		);

		PaymentCreatedResult result = createPaymentUseCase.create(command);

		return ResponseEntity.ok(CommonResponse.success(
			PaymentChargeResponse.from(result, requestedAmount))
		);
	}

	@Override
	@PostMapping("/confirm")
	public ResponseEntity<CommonResponse<PaymentConfirmResponse>> confirm(
		@CurrentMemberId Long memberId,
		@Valid @RequestBody PaymentConfirmRequest request
	) {
		log.info("[PaymentController] 결제 승인 요청. memberId={}, paymentId={}", memberId, request.paymentId());

		// 1. 원본 Payment 조회
		Payment payment = paymentRepository.findById(request.paymentId())
			.orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

		// 2. 금액 검증 (조작 방지)
		Money requestedAmount = Money.of(request.amount());
		if (!payment.getPaidAmount().equals(requestedAmount)) {
			log.warn("[PaymentController] 금액 불일치! expected={}, actual={}",
				payment.getPaidAmount(), requestedAmount);
			throw new PaymentException(PaymentErrorCode.AMOUNT_MISMATCH);
		}

		// 3. 소유자 검증
		if (!payment.getMemberId().equals(memberId)) {
			log.warn("[PaymentController] 결제 소유자 불일치! paymentMemberId={}, requestMemberId={}",
				payment.getMemberId(), memberId);
			throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND);
		}

		// 4. Toss PG 승인 요청 (DB에서 조회한 금액 사용)
		TossConfirmResult pgResult = paymentGateway.confirm(
			request.paymentKey(),
			request.orderId(),
			payment.getPaidAmount()
		);

		if (pgResult.success()) {
			ConfirmPaymentCommand command = new ConfirmPaymentCommand(
				request.paymentId(),
				request.paymentKey(),
				null,
				LocalDateTime.now()
			);
			confirmPaymentUseCase.confirm(command);

			return ResponseEntity.ok(CommonResponse.success(
				PaymentConfirmResponse.success(request.paymentId())
			));
		} else {
			return ResponseEntity.ok(CommonResponse.success(
				PaymentConfirmResponse.failure(pgResult.errorCode(), pgResult.errorMessage())
			));
		}
	}
}
