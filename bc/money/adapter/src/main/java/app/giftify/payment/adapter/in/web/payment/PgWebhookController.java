package app.giftify.payment.adapter.in.web.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.giftify.payment.adapter.in.web.payment.dto.PgCancelWebhookRequest;
import domain.payment.CancelReason;
import domain.payment.Payment;
import domain.payment.PaymentErrorCode;
import domain.payment.PaymentException;
import domain.payment.PaymentRepository;
import payment.usecase.PaymentCancelUseCase;
import payment.usecase.command.CancelPaymentCommand;

@RestController
@RequestMapping("/api/payments/webhook")
public class PgWebhookController {
	private static final Logger log = LoggerFactory.getLogger(PgWebhookController.class);

	private final PaymentCancelUseCase paymentCancelUseCase;
	private final PaymentRepository paymentRepository;
	private final PgWebhookValidator pgWebhookValidator;
	private final ObjectMapper objectMapper;

	public PgWebhookController(
		PaymentCancelUseCase paymentCancelUseCase, PaymentRepository paymentRepository,
		PgWebhookValidator pgWebhookValidator,
		ObjectMapper objectMapper
	) {
		this.paymentCancelUseCase = paymentCancelUseCase;
		this.paymentRepository = paymentRepository;
		this.pgWebhookValidator = pgWebhookValidator;
		this.objectMapper = objectMapper;
	}

	@PostMapping("/pg/cancel")
	public ResponseEntity<Void> handlePgCancel(
		@RequestBody String rawPayload,
		@RequestHeader("tosspayments-webhook-transmission-time") String timestamp,
		@RequestHeader("tosspayments-webhook-signature") String signature
	) {
		// 1. 서명 검증 (원본 문자열 사용)
		if (!pgWebhookValidator.validate(rawPayload, timestamp, signature)) {
			log.warn("[Payment] 유효하지 않은 PG 웹훅 서명입니다.");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		// 2. 수동 파싱
		PgCancelWebhookRequest request;
		try {
			request = objectMapper.readValue(rawPayload, PgCancelWebhookRequest.class);
		} catch (JsonProcessingException e) {
			log.error("[Payment] 웹훅 Payload 파싱 실패", e);
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE, "Invalid Webhook Payload");
		}

		// 3. 비즈니스 로직
		Payment payment = paymentRepository.findByPgTransactionId(request.pgTransactionId())
			.orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND,
				"[Payment] 해당 PG 거래건을 찾을 수 없습니다: " + request.pgTransactionId()));

		paymentCancelUseCase.cancel(new CancelPaymentCommand(
			payment.getPaymentId(),
			null, // 시스템(PG)에 의한 취소
			CancelReason.PG_CANCELED,
			rawPayload // 원본 데이터를 그대로 메타데이터로 저장
		));

		return ResponseEntity.ok().build();
	}
}
