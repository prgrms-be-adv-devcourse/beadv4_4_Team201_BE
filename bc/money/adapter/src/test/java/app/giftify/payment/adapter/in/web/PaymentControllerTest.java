package app.giftify.payment.adapter.in.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.giftify.payment.adapter.in.web.exception.MoneyGlobalExceptionHandler;
import app.giftify.payment.adapter.in.web.payment.PaymentController;
import app.giftify.payment.adapter.in.web.payment.dto.PaymentChargeRequest;
import app.giftify.payment.adapter.in.web.payment.dto.PaymentConfirmRequest;
import app.giftify.payment.adapter.out.pg.TossConfirmResult;
import app.giftify.payment.adapter.out.pg.TossPaymentsClient;
import app.giftify.shared.domain.vo.Money;
import domain.payment.Payment;
import domain.payment.PaymentRepository;
import domain.payment.PaymentStatus;
import payment.usecase.PaymentChargeUseCase;
import payment.usecase.PaymentCompleteUseCase;
import payment.usecase.result.PaymentResult;

@WebMvcTest(PaymentController.class)
@Import(MoneyGlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false) // Security 필터 비활성화
@org.springframework.test.context.ContextConfiguration(classes = {PaymentController.class})
@DisplayName("PaymentController 테스트")
class PaymentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private PaymentChargeUseCase paymentChargeUseCase;

	@MockBean
	private PaymentCompleteUseCase paymentCompleteUseCase;

	@MockBean
	private PaymentRepository paymentRepository;

	@MockBean
	private TossPaymentsClient tossPaymentsClient;

	@Nested
	@DisplayName("POST /api/payments/charge")
	class ChargeTest {

		@Test
		@DisplayName("충전 요청 성공 시 PENDING 상태의 Payment 정보를 반환한다")
		void charge_ShouldReturnPendingPayment_WhenValidRequest() throws Exception {
			// Given
			BigDecimal amount = new BigDecimal("10000");
			PaymentChargeRequest request = new PaymentChargeRequest(amount);

			PaymentResult result = new PaymentResult(
				1L,
				"GFTFY_CHARGE_test123uuid",
				PaymentStatus.PENDING,
				Money.of(amount)
			);

			given(paymentChargeUseCase.charge(any())).willReturn(result);

			// When & Then
			mockMvc.perform(post("/api/payments/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("SUCCESS"))
				.andExpect(jsonPath("$.data.paymentId").value(1))
				.andExpect(jsonPath("$.data.orderId").value("GFTFY_CHARGE_test123uuid"))
				.andExpect(jsonPath("$.data.status").value("PENDING"))
				.andExpect(jsonPath("$.data.amount").value(10000))
				.andExpect(jsonPath("$.data.orderName").value("Giftify 캐시 충전"));
		}

		@Test
		@DisplayName("최소 금액 미만 요청 시 400 Bad Request를 반환한다")
		void charge_ShouldReturnBadRequest_WhenAmountBelowMinimum() throws Exception {
			// Given
			PaymentChargeRequest request = new PaymentChargeRequest(new BigDecimal("500"));

			// When & Then
			mockMvc.perform(post("/api/payments/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("POST /api/payments/confirm")
	class ConfirmTest {

		@Test
		@DisplayName("결제 승인 성공 시 PAID 상태를 반환한다")
		void confirm_ShouldReturnPaidStatus_WhenTossApprovalSucceeds() throws Exception {
			// Given
			Long paymentId = 1L;
			String orderId = "GFTFY_CHARGE_test123uuid";
			String paymentKey = "toss_payment_key_xxx";
			BigDecimal amount = new BigDecimal("10000");

			PaymentConfirmRequest request = new PaymentConfirmRequest(paymentKey, paymentId, amount);

			Payment payment = Payment.builder()
				.paymentId(paymentId)
				.orderId(orderId)
				.userId(100L)
				.status(PaymentStatus.PENDING)
				.amount(Money.of(amount))
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(tossPaymentsClient.confirm(eq(paymentKey), eq(orderId), eq(amount)))
				.willReturn(TossConfirmResult.success(paymentKey));

			// When & Then
			mockMvc.perform(post("/api/payments/confirm")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.requestAttr("memberId", 100L)) // @CurrentMemberId 시뮬레이션
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("SUCCESS"))
				.andExpect(jsonPath("$.data.paymentId").value(1))
				.andExpect(jsonPath("$.data.status").value("PAID"));

			verify(paymentCompleteUseCase).complete(paymentId, paymentKey, true);
		}

		@Test
		@DisplayName("결제 정보를 찾을 수 없으면 400 Bad Request를 반환한다")
		void confirm_ShouldReturnBadRequest_WhenPaymentNotFound() throws Exception {
			// Given
			PaymentConfirmRequest request = new PaymentConfirmRequest(
				"paymentKey", 999L, new BigDecimal("10000")
			);

			given(paymentRepository.findById(999L)).willReturn(Optional.empty());

			// When & Then
			mockMvc.perform(post("/api/payments/confirm")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("금액 불일치 시 400 Bad Request를 반환한다")
		void confirm_ShouldReturnBadRequest_WhenAmountMismatch() throws Exception {
			// Given
			Long paymentId = 1L;
			PaymentConfirmRequest request = new PaymentConfirmRequest(
				"paymentKey", paymentId, new BigDecimal("20000") // 다른 금액
			);

			Payment payment = Payment.builder()
				.paymentId(paymentId)
				.orderId("GFTFY_CHARGE_test")
				.userId(100L)
				.status(PaymentStatus.PENDING)
				.amount(Money.of(new BigDecimal("10000"))) // 원래 금액
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

			// When & Then
			mockMvc.perform(post("/api/payments/confirm")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Toss 승인 실패 시 400 Bad Request를 반환한다")
		void confirm_ShouldReturnBadRequest_WhenTossApprovalFails() throws Exception {
			// Given
			Long paymentId = 1L;
			String orderId = "GFTFY_CHARGE_test";
			String paymentKey = "paymentKey";
			BigDecimal amount = new BigDecimal("10000");

			PaymentConfirmRequest request = new PaymentConfirmRequest(paymentKey, paymentId, amount);

			Payment payment = Payment.builder()
				.paymentId(paymentId)
				.orderId(orderId)
				.userId(100L)
				.status(PaymentStatus.PENDING)
				.amount(Money.of(amount))
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(tossPaymentsClient.confirm(eq(paymentKey), eq(orderId), eq(amount)))
				.willReturn(TossConfirmResult.failure("INVALID_CARD", "카드 정보가 유효하지 않습니다"));

			// When & Then
			mockMvc.perform(post("/api/payments/confirm")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());

			verify(paymentCompleteUseCase).complete(paymentId, paymentKey, false);
		}
	}
}