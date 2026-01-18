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
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.giftify.payment.adapter.in.web.dto.PgCancelWebhookRequest;
import app.giftify.payment.adapter.in.web.exception.MoneyGlobalExceptionHandler;
import domain.payment.Payment;
import domain.payment.PaymentRepository;
import domain.payment.PaymentStatus;
import payment.usecase.PaymentCancelUseCase;
import payment.usecase.command.CancelPaymentCommand;

@WebMvcTest(PgWebhookController.class)
@ContextConfiguration(classes = {PgWebhookController.class, MoneyGlobalExceptionHandler.class})
class PgWebhookControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private PaymentCancelUseCase paymentCancelUseCase;

	@MockBean
	private PaymentRepository paymentRepository;

	@Test
	@DisplayName("PG 취소 웹훅: 유효한 거래 ID가 오면 결제 취소 유즈케이스를 호출한다")
	void handlePgCancel_ShouldCallCancelUseCase_WhenValidRequest() throws Exception {
		// Given
		String pgTxId = "pg_tx_12345";
		PgCancelWebhookRequest request = new PgCancelWebhookRequest(pgTxId, "고객 변심", "2024-01-01T12:00:00");

		Payment payment = Payment.builder()
			.paymentId(100L)
			.pgTransactionId(pgTxId)
			.status(PaymentStatus.PAID)
			.build();

		given(paymentRepository.findByPgTransactionId(pgTxId)).willReturn(Optional.of(payment));

		// When & Then
		mockMvc.perform(post("/api/payments/webhook/pg/cancel")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());

		verify(paymentCancelUseCase).cancel(any(CancelPaymentCommand.class));
	}

	@Test
	@DisplayName("PG 취소 웹훅: 존재하지 않는 거래 ID면 400 에러(또는 예외)를 반환한다")
	void handlePgCancel_ShouldReturnError_WhenPaymentNotFound() throws Exception {
		// Given
		String invalidPgTxId = "invalid_id";
		PgCancelWebhookRequest request = new PgCancelWebhookRequest(invalidPgTxId, "사유", "2024-01-01T12:00:00");

		given(paymentRepository.findByPgTransactionId(invalidPgTxId)).willReturn(Optional.empty());

		// When & Then
		mockMvc.perform(post("/api/payments/webhook/pg/cancel")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}
}
