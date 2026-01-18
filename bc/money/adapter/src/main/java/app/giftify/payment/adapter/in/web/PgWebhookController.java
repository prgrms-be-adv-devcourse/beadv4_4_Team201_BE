package app.giftify.payment.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.payment.adapter.in.web.dto.PgCancelWebhookRequest;
import domain.payment.CancelReason;
import domain.payment.Payment;
import domain.payment.PaymentRepository;
import payment.usecase.PaymentCancelUseCase;
import payment.usecase.command.CancelPaymentCommand;

@RestController
@RequestMapping("/api/payments/webhook")
public class PgWebhookController {
	private final PaymentCancelUseCase paymentCancelUseCase;
	private final PaymentRepository paymentRepository;

	public PgWebhookController(PaymentCancelUseCase paymentCancelUseCase, PaymentRepository paymentRepository) {
		this.paymentCancelUseCase = paymentCancelUseCase;
		this.paymentRepository = paymentRepository;
	}

	@PostMapping("/pg/cancel")
	public ResponseEntity<Void> handlePgCancel(@RequestBody PgCancelWebhookRequest request) {
		// PG사 식별자(pgTransactionId)로 결제 건을 조회
		Payment payment = paymentRepository.findByPgTransactionId(request.pgTransactionId())
			.orElseThrow(
				() -> new IllegalArgumentException("[Payment] 해당 PG 거래건을 찾을 수 없습니다: " + request.pgTransactionId()));

		paymentCancelUseCase.cancel(new CancelPaymentCommand(
			payment.getPaymentId(),
			null, // PG사 시스템에 의한 취소를 null 로 표현
			CancelReason.PG_CANCELED
		));

		return ResponseEntity.ok().build();
	}
}
