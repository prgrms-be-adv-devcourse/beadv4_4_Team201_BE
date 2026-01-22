package app.giftify.payment.adapter.in.web.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.payment.adapter.in.web.payment.dto.PaymentChargeRequest;
import app.giftify.payment.adapter.in.web.payment.dto.PaymentChargeResponse;
import app.giftify.payment.adapter.in.web.payment.dto.PaymentConfirmRequest;
import app.giftify.payment.adapter.in.web.payment.dto.PaymentConfirmResponse;
import app.giftify.payment.adapter.out.pg.TossConfirmResult;
import app.giftify.payment.adapter.out.pg.TossPaymentsClient;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.CommonResponse;
import app.giftify.shared.domain.vo.Money;
import domain.payment.Payment;
import domain.payment.PaymentErrorCode;
import domain.payment.PaymentException;
import domain.payment.PaymentRepository;
import jakarta.validation.Valid;
import payment.usecase.PaymentChargeUseCase;
import payment.usecase.PaymentCompleteUseCase;
import payment.usecase.command.PaymentChargeCommand;
import payment.usecase.result.PaymentResult;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

	private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

	private final PaymentChargeUseCase paymentChargeUseCase;
	private final PaymentCompleteUseCase paymentCompleteUseCase;
	private final PaymentRepository paymentRepository;
	private final TossPaymentsClient tossPaymentsClient;

	public PaymentController(
		PaymentChargeUseCase paymentChargeUseCase,
		PaymentCompleteUseCase paymentCompleteUseCase,
		PaymentRepository paymentRepository,
		TossPaymentsClient tossPaymentsClient
	) {
		this.paymentChargeUseCase = paymentChargeUseCase;
		this.paymentCompleteUseCase = paymentCompleteUseCase;
		this.paymentRepository = paymentRepository;
		this.tossPaymentsClient = tossPaymentsClient;
	}

	/**
	 * 캐시 충전을 위한 결제 요청을 생성합니다.
	 * 클라이언트는 응답으로 받은 orderId를 Toss SDK에 전달하여 결제를 진행합니다.
	 */
	@PostMapping("/charge")
	public ResponseEntity<CommonResponse<PaymentChargeResponse>> charge(
		@CurrentMemberId Long memberId,
		@Valid @RequestBody PaymentChargeRequest request
	) {
		PaymentChargeCommand command = new PaymentChargeCommand(
			memberId,
			Money.of(request.amount())
		);

		PaymentResult result = paymentChargeUseCase.charge(command);

		PaymentChargeResponse response = PaymentChargeResponse.of(
			result.paymentId(),
			result.orderId(),
			result.amount().amount(),
			result.status()
		);

		return ResponseEntity.ok(CommonResponse.success(response));
	}

	/**
	 * Toss SDK 결제 완료 후 서버 측 승인을 처리합니다.
	 * 1. DB에서 Payment 조회 (저장된 orderId 사용)
	 * 2. 금액 검증
	 * 3. Toss confirm API 호출
	 * 4. 결제 상태 업데이트 (PAID)
	 */
	@PostMapping("/confirm")
	public ResponseEntity<CommonResponse<PaymentConfirmResponse>> confirm(
		@CurrentMemberId Long memberId,
		@Valid @RequestBody PaymentConfirmRequest request
	) {
		// 1. Payment 조회
		Payment payment = paymentRepository.findById(request.paymentId())
			.orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND,
				"결제 정보를 찾을 수 없습니다: " + request.paymentId()));

		// 2. 소유자 검증
		if (!payment.getUserId().equals(memberId)) {
			log.warn("[Payment] 결제 소유자 불일치. paymentId={}, requestUserId={}, actualUserId={}",
				request.paymentId(), memberId, payment.getUserId());
			throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND, "결제 정보를 찾을 수 없습니다");
		}

		// 3. 금액 검증
		if (payment.getAmount().amount().compareTo(request.amount()) != 0) {
			log.warn("[Payment] 금액 불일치. paymentId={}, expected={}, actual={}",
				request.paymentId(), payment.getAmount().amount(), request.amount());
			throw new PaymentException(PaymentErrorCode.AMOUNT_MISMATCH,
				"결제 금액이 일치하지 않습니다");
		}

		// 4. Toss confirm API 호출 (서버에 저장된 orderId 사용)
		TossConfirmResult confirmResult = tossPaymentsClient.confirm(
			request.paymentKey(),
			payment.getOrderId(),
			request.amount()
		);

		// 5. 결제 완료 처리
		if (confirmResult.success()) {
			paymentCompleteUseCase.complete(
				payment.getPaymentId(),
				request.paymentKey(),
				true
			);

			PaymentConfirmResponse response = new PaymentConfirmResponse(
				payment.getPaymentId(),
				domain.payment.PaymentStatus.PAID
			);
			return ResponseEntity.ok(CommonResponse.success(response));
		} else {
			paymentCompleteUseCase.complete(
				payment.getPaymentId(),
				request.paymentKey(),
				false
			);

			throw new PaymentException(PaymentErrorCode.PG_APPROVAL_FAILED,
				String.format("결제 승인 실패 [%s]: %s",
					confirmResult.errorCode(), confirmResult.errorMessage()));
		}
	}
}
