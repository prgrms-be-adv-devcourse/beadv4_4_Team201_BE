package app.giftify.payment.adapter.inbound.web;

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
import app.giftify.payment.application.inbound.ConfirmPaymentCommand;
import app.giftify.payment.application.inbound.ConfirmPaymentResult;
import app.giftify.payment.application.inbound.ConfirmPaymentUseCase;
import app.giftify.payment.application.inbound.CreatePaymentCommand;
import app.giftify.payment.application.inbound.CreatePaymentUseCase;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.application.inbound.QueryPaymentUseCase;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.RsData;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v2/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentV2ApiSpec {
	private final CreatePaymentUseCase createPaymentUseCase;
	private final ConfirmPaymentUseCase confirmPaymentUseCase;
	private final QueryPaymentUseCase queryPaymentUseCase;

	@Override
	@PostMapping("/charge")
	public ResponseEntity<RsData<PaymentChargeResponse>> charge(
		@CurrentMemberId Long memberId,
		@Valid @RequestBody PaymentChargeRequest request
	) {
		log.info("[PaymentController] 결제 생성 요청. memberId={}, amount={}", memberId, request.amount());

		String orderId = request.orderId() != null ? request.orderId() : "CHG-" + System.currentTimeMillis();
		String idempotencyKey = UUID.randomUUID().toString(); // 주문이 없으므로 별도의 멱등성 키 생성 필요
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
