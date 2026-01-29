package app.giftify.payment.adapter.inbound.web;

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
import app.giftify.payment.domain.PaymentMethod;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.CommonResponse;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v2/payments")
@RequiredArgsConstructor
public class PaymentController {
	private final CreatePaymentUseCase createPaymentUseCase;
	private final ConfirmPaymentUseCase confirmPaymentUseCase;
	private final QueryPaymentUseCase queryPaymentUseCase;
	private final PaymentGateway paymentGateway;

	@PostMapping("/charge")
	public ResponseEntity<CommonResponse<PaymentChargeResponse>> charge(
		@CurrentMemberId Long memberId,
		@Valid @RequestBody PaymentChargeRequest request
	) {
		log.info("[PaymentController] 결제 생성 요청. memberId={}, amount={}", memberId, request.amount());

		String orderId = request.orderId() != null ? request.orderId() : "CHG-" + UUID.randomUUID();
		String idempotencyKey = UUID.randomUUID().toString();

		CreatePaymentCommand command = new CreatePaymentCommand(
			idempotencyKey,
			memberId,
			orderId,
			PaymentType.POINT_CHARGE,
			PaymentMethod.CARD,
			Money.of(request.amount()),
			Collections.emptyList()
		);

		PaymentCreatedResult result = createPaymentUseCase.create(command);

		return ResponseEntity.ok(CommonResponse.success(PaymentChargeResponse.from(result)));
	}

	@PostMapping("/confirm")
	public ResponseEntity<CommonResponse<PaymentConfirmResponse>> confirm(
		@CurrentMemberId Long memberId,
		@Valid @RequestBody PaymentConfirmRequest request
	) {
		log.info("[PaymentController] 결제 승인 요청. memberId={}, paymentId={}", memberId, request.paymentId());

		TossConfirmResult pgResult = paymentGateway.confirm(
			request.paymentKey(),
			request.orderId(),
			Money.of(request.amount())
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
