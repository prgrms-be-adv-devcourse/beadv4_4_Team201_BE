package app.giftify.payment.adapter.in.web;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.giftify.payment.adapter.in.web.exception.MoneyGlobalExceptionHandler;
import app.giftify.payment.adapter.in.web.payment.PgWebhookController;
import app.giftify.payment.adapter.in.web.payment.PgWebhookValidator;
import app.giftify.payment.adapter.in.web.payment.dto.PgCancelWebhookRequest;
import domain.payment.Payment;
import domain.payment.PaymentRepository;
import domain.payment.PaymentStatus;
import payment.usecase.PaymentCancelUseCase;

@WebMvcTest(PgWebhookController.class)
@Import(MoneyGlobalExceptionHandler.class)
@ContextConfiguration(classes = {PgWebhookController.class})
class PgWebhookControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private PaymentCancelUseCase paymentCancelUseCase;

	@MockBean
	private PaymentRepository paymentRepository;

	@MockBean
	private PgWebhookValidator pgWebhookValidator;

	@Test
	@DisplayName("PG 취소 웹훅: 서명이 유효하고 결제 건이 존재하면 취소 유즈케이스를 호출한다")
	void handlePgCancel_ShouldCallCancelUseCase_WhenValidRequest() throws Exception {
		// Given
		String pgTxId = "pg_tx_12345";
		String timestamp = "2024-01-01T12:00:00";
		String signature = "v1:dummy-signature";
		PgCancelWebhookRequest requestDto = new PgCancelWebhookRequest(pgTxId, "고객 변심", timestamp);
		String rawPayload = objectMapper.writeValueAsString(requestDto);

		Payment payment = Payment.builder()
			.paymentId(100L)
			.pgTransactionId(pgTxId)
			.status(PaymentStatus.PAID)
			.build();

		// Mocking: 서명 검증 통과
		given(pgWebhookValidator.validate(anyString(), eq(timestamp), eq(signature))).willReturn(true);
		// Mocking: 결제 건 조회 성공
		given(paymentRepository.findByPgTransactionId(pgTxId)).willReturn(Optional.of(payment));

		// When & Then
		mockMvc.perform(post("/api/payments/webhook/pg/cancel")
				.header("tosspayments-webhook-transmission-time", timestamp)
				.header("tosspayments-webhook-signature", signature)
				.contentType(MediaType.APPLICATION_JSON)
				.content(rawPayload))
			.andExpect(status().isOk());

		// UseCase가 올바른 파라미터(rawPayload 포함)로 호출되었는지 검증
		verify(paymentCancelUseCase).cancel(argThat(command ->
			command.paymentId().equals(100L) &&
				command.metadata().equals(rawPayload)
		));
	}

	@Test
	@DisplayName("PG 취소 웹훅: 서명이 유효하지 않으면 403 Forbidden을 반환한다")
	void handlePgCancel_ShouldReturnForbidden_WhenInvalidSignature() throws Exception {
		// Given
		given(pgWebhookValidator.validate(any(), any(), any())).willReturn(false);

		// When & Then
		mockMvc.perform(post("/api/payments/webhook/pg/cancel")
				.header("tosspayments-webhook-transmission-time", "any")
				.header("tosspayments-webhook-signature", "wrong")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isForbidden());

		verify(paymentCancelUseCase, never()).cancel(any());
	}

	@Test
	@DisplayName("PG 취소 웹훅: 결제 건을 찾을 수 없으면 400 Bad Request를 반환한다")
	void handlePgCancel_ShouldReturnBadRequest_WhenPaymentNotFound() throws Exception {
		// Given
		String pgTxId = "not_found_id";
		PgCancelWebhookRequest requestDto = new PgCancelWebhookRequest(pgTxId, "사유", "time");
		String rawPayload = objectMapper.writeValueAsString(requestDto);

		given(pgWebhookValidator.validate(any(), any(), any())).willReturn(true);
		given(paymentRepository.findByPgTransactionId(pgTxId)).willReturn(Optional.empty());

		// When & Then
		mockMvc.perform(post("/api/payments/webhook/pg/cancel")
				.header("tosspayments-webhook-transmission-time", "time")
				.header("tosspayments-webhook-signature", "sig")
				.contentType(MediaType.APPLICATION_JSON)
				.content(rawPayload))
			.andExpect(status().isBadRequest());
	}
}
