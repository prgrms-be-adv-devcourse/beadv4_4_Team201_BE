package app.giftify.payment.adapter.in.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.giftify.payment.adapter.in.web.exception.MoneyGlobalExceptionHandler;
import app.giftify.payment.adapter.in.web.payment.PgWebhookController;
import app.giftify.payment.adapter.in.web.payment.PgWebhookValidator;
import app.giftify.payment.adapter.in.web.payment.dto.PgCancelWebhookRequest;
import domain.payment.Payment;
import domain.payment.PaymentRepository;
import domain.payment.PaymentStatus;
import payment.usecase.PaymentCancelUseCase;

/**
 * PgWebhookController 테스트.
 *
 * <p>standaloneSetup을 사용하여 Spring Security 필터 없이 컨트롤러 로직만 테스트</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PgWebhookController 테스트")
class PgWebhookControllerTest {

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@Mock
	private PaymentCancelUseCase paymentCancelUseCase;

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
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
			.orderUuid("GFTFY_CHARGE_test123")
			.paymentKey(pgTxId)
			.status(PaymentStatus.PAID)
			.build();

		// Mocking: 서명 검증 통과
		given(pgWebhookValidator.validate(any(), any(), any())).willReturn(true);
		// Mocking: 결제 건 조회 성공
		given(paymentRepository.findByPgTransactionId(any())).willReturn(Optional.of(payment));

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

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();

		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
		jsonConverter.setObjectMapper(objectMapper);

		// StringHttpMessageConverter를 먼저 등록해야 @RequestBody String이 올바르게 처리됨
		StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();

		PgWebhookController controller = new PgWebhookController(
			paymentCancelUseCase,
			paymentRepository,
			pgWebhookValidator,
			objectMapper
		);

		mockMvc = MockMvcBuilders
			.standaloneSetup(controller)
			.setMessageConverters(stringConverter, jsonConverter)
			.setControllerAdvice(new MoneyGlobalExceptionHandler())
			.build();
	}
}
