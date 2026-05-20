package app.giftify.payment.adapter.inbound.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.payment.adapter.inbound.web.dto.PaymentChargeRequest;
import app.giftify.payment.adapter.inbound.web.dto.PaymentChargeResponse;
import app.giftify.payment.adapter.inbound.web.dto.PaymentConfirmRequest;
import app.giftify.payment.adapter.inbound.web.dto.PaymentConfirmResponse;
import app.giftify.payment.application.inbound.ChargeDepositCommand;
import app.giftify.payment.application.inbound.ChargeDepositUseCase;
import app.giftify.payment.application.inbound.ConfirmPaymentCommand;
import app.giftify.payment.application.inbound.ConfirmPaymentResult;
import app.giftify.payment.application.inbound.ConfirmPaymentUseCase;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.application.inbound.QueryPaymentUseCase;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.RsData;
import app.giftify.shared.domain.vo.Money;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/v2/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentV2ApiSpec {
	private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

	private final ChargeDepositUseCase chargeDepositUseCase;
	private final ConfirmPaymentUseCase confirmPaymentUseCase;
	private final QueryPaymentUseCase queryPaymentUseCase;

	@Override
	@PostMapping("/charge")
	public ResponseEntity<RsData<PaymentChargeResponse>> charge(
		@CurrentMemberId Long memberId,
		@Valid @RequestBody PaymentChargeRequest request
	) {
		log.info("[PaymentController] 예치금 충전 요청. memberId={}, amount={}, orderId={}",
			memberId, request.amount(), request.orderId());

		var requestedAmount = Money.of(request.amount());

		// orderId가 멱등성 키 역할을 함 (프론트에서 생성하여 전송)
		ChargeDepositCommand command = new ChargeDepositCommand(
			memberId,
			request.orderId(),
			requestedAmount
		);

		PaymentCreatedResult result = chargeDepositUseCase.charge(command);

		return ResponseEntity.ok(RsData.success(
			PaymentChargeResponse.from(result, requestedAmount))
		);
	}

	@Override
	@PostMapping("/confirm")
	public ResponseEntity<RsData<PaymentConfirmResponse>> confirm(
		@CurrentMemberId Long memberId,
		@Valid @RequestBody PaymentConfirmRequest request
	) {
		log.info("[PaymentController] 결제 승인 요청. memberId={}, paymentId={}", memberId, request.paymentId());

		ConfirmPaymentCommand command = new ConfirmPaymentCommand(
			request.paymentId(),
			memberId,
			request.paymentKey(),
			request.orderId(),
			Money.of(request.amount())
		);

		ConfirmPaymentResult result = confirmPaymentUseCase.confirm(command);

		if (result.success()) {
			return ResponseEntity.ok(RsData.success(
				PaymentConfirmResponse.success(result.paymentId())
			));
		} else {
			return ResponseEntity.ok(RsData.success(
				PaymentConfirmResponse.failure(result.errorCode(), result.errorMessage())
			));
		}
	}
}
