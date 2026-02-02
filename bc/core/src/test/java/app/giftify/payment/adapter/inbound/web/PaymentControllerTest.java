package app.giftify.payment.adapter.inbound.web;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.giftify.payment.adapter.inbound.web.dto.PaymentChargeRequest;
import app.giftify.payment.adapter.inbound.web.dto.PaymentConfirmRequest;
import app.giftify.payment.adapter.inbound.web.exception.PaymentExceptionHandler;
import app.giftify.payment.adapter.outbound.pg.TossConfirmResult;
import app.giftify.payment.application.inbound.ConfirmPaymentCommand;
import app.giftify.payment.application.inbound.ConfirmPaymentUseCase;
import app.giftify.payment.application.inbound.CreatePaymentCommand;
import app.giftify.payment.application.inbound.CreatePaymentUseCase;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.application.inbound.QueryPaymentUseCase;
import app.giftify.payment.application.outbound.PaymentGateway;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentMethod;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentController 테스트")
class PaymentControllerTest {

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;

	@Mock
	private CreatePaymentUseCase createPaymentUseCase;

	@Mock
	private ConfirmPaymentUseCase confirmPaymentUseCase;

	@Mock
	private QueryPaymentUseCase queryPaymentUseCase;

	@Mock
	private PaymentGateway paymentGateway;

	@Mock
	private PaymentRepository paymentRepository;

	private static final Long TEST_MEMBER_ID = 100L;
	private static final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(10000);

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		mockMvc = MockMvcBuilders
			.standaloneSetup(new PaymentController(
				createPaymentUseCase,
				confirmPaymentUseCase,
				queryPaymentUseCase,
				paymentGateway,
				paymentRepository
			))
			.setControllerAdvice(new PaymentExceptionHandler())
			.setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
			.setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
				@Override
				public boolean supportsParameter(MethodParameter parameter) {
					return parameter.hasParameterAnnotation(CurrentMemberId.class);
				}

				@Override
				public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
					NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
					return TEST_MEMBER_ID;
				}
			})
			.build();
	}

	@Nested
	@DisplayName("charge 메서드")
	class ChargeTests {

		@Test
		@DisplayName("orderId를 제공하면 해당 orderId로 결제를 생성한다")
		void charge_WithProvidedOrderId_Success() throws Exception {
			// given
			String providedOrderId = "ORDER-123456";
			PaymentChargeRequest request = new PaymentChargeRequest(TEST_AMOUNT, providedOrderId, null);

			PaymentCreatedResult result = new PaymentCreatedResult(
				1L,
				providedOrderId,
				"idempotency-key-123",
				PaymentStatus.PENDING,
				true
			);

			given(createPaymentUseCase.create(any(CreatePaymentCommand.class))).willReturn(result);

			// when & then
			mockMvc.perform(post("/api/v2/payments/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("SUCCESS"))
				.andExpect(jsonPath("$.data.paymentId").value(1))
				.andExpect(jsonPath("$.data.orderId").value(providedOrderId))
				.andExpect(jsonPath("$.data.amount").value(10000))
				.andExpect(jsonPath("$.data.idempotencyKey").value("idempotency-key-123"))
				.andExpect(jsonPath("$.data.status").value("PENDING"));

			verify(createPaymentUseCase).create(any(CreatePaymentCommand.class));
		}

		@Test
		@DisplayName("orderId를 제공하지 않으면 CHG-UUID 형식의 orderId를 생성한다")
		void charge_WithoutOrderId_GeneratesOrderId() throws Exception {
			// given
			PaymentChargeRequest request = new PaymentChargeRequest(TEST_AMOUNT, null, null);

			PaymentCreatedResult result = new PaymentCreatedResult(
				1L,
				"CHG-generated-uuid",
				"idempotency-key-123",
				PaymentStatus.PENDING,
				true
			);

			given(createPaymentUseCase.create(any(CreatePaymentCommand.class))).willReturn(result);

			// when & then
			mockMvc.perform(post("/api/v2/payments/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("SUCCESS"))
				.andExpect(jsonPath("$.data.paymentId").value(1))
				.andExpect(jsonPath("$.data.orderId").value(startsWith("CHG-")))
				.andExpect(jsonPath("$.data.amount").value(10000))
				.andExpect(jsonPath("$.data.status").value("PENDING"));

			verify(createPaymentUseCase).create(any(CreatePaymentCommand.class));
		}

		@Test
		@DisplayName("유효하지 않은 금액으로 요청하면 400 에러를 반환한다")
		void charge_WithInvalidAmount_ReturnsBadRequest() throws Exception {
			// given
			PaymentChargeRequest request = new PaymentChargeRequest(BigDecimal.ZERO, null, null);

			// when & then
			mockMvc.perform(post("/api/v2/payments/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isBadRequest());

			verify(createPaymentUseCase, never()).create(any(CreatePaymentCommand.class));
		}

		@Test
		@DisplayName("null 금액으로 요청하면 400 에러를 반환한다")
		void charge_WithNullAmount_ReturnsBadRequest() throws Exception {
			// given
			String requestJson = "{\"orderId\":\"ORDER-123\",\"paymentType\":null}";

			// when & then
			mockMvc.perform(post("/api/v2/payments/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson))
				.andDo(print())
				.andExpect(status().isBadRequest());

			verify(createPaymentUseCase, never()).create(any(CreatePaymentCommand.class));
		}
	}

	@Nested
	@DisplayName("confirm 메서드")
	class ConfirmTests {

		@Test
		@DisplayName("정상적인 PG 승인 요청이면 결제를 승인한다")
		void confirm_WithValidRequest_Success() throws Exception {
			// given
			Long paymentId = 1L;
			String paymentKey = "toss-payment-key-123";
			String orderId = "ORDER-123456";

			PaymentConfirmRequest request = new PaymentConfirmRequest(
				paymentId,
				paymentKey,
				orderId,
				TEST_AMOUNT
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idempotency-key")
				.orderId(orderId)
				.memberId(TEST_MEMBER_ID)
				.type(PaymentType.POINT_CHARGE)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(TEST_AMOUNT))
				.paidAmount(Money.of(TEST_AMOUNT))
				.orderItems(Collections.emptyList())
				.status(PaymentStatus.PENDING)
				.build();

			TossConfirmResult pgResult = TossConfirmResult.success(paymentKey);

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentGateway.confirm(eq(paymentKey), eq(orderId), any(Money.class)))
				.willReturn(pgResult);

			// when & then
			mockMvc.perform(post("/api/v2/payments/confirm")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("SUCCESS"))
				.andExpect(jsonPath("$.data.paymentId").value(paymentId))
				.andExpect(jsonPath("$.data.success").value(true))
				.andExpect(jsonPath("$.data.errorCode").isEmpty())
				.andExpect(jsonPath("$.data.errorMessage").isEmpty());

			verify(paymentRepository).findById(paymentId);
			verify(paymentGateway).confirm(eq(paymentKey), eq(orderId), any(Money.class));
			verify(confirmPaymentUseCase).confirm(any(ConfirmPaymentCommand.class));
		}

		@Test
		@DisplayName("PG 승인이 실패하면 에러 정보를 반환한다")
		void confirm_PgConfirmFailed_ReturnsFailureResponse() throws Exception {
			// given
			Long paymentId = 1L;
			String paymentKey = "toss-payment-key-123";
			String orderId = "ORDER-123456";
			String errorCode = "INVALID_CARD";
			String errorMessage = "카드 정보가 올바르지 않습니다";

			PaymentConfirmRequest request = new PaymentConfirmRequest(
				paymentId,
				paymentKey,
				orderId,
				TEST_AMOUNT
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idempotency-key")
				.orderId(orderId)
				.memberId(TEST_MEMBER_ID)
				.type(PaymentType.POINT_CHARGE)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(TEST_AMOUNT))
				.paidAmount(Money.of(TEST_AMOUNT))
				.orderItems(Collections.emptyList())
				.status(PaymentStatus.PENDING)
				.build();

			TossConfirmResult pgResult = TossConfirmResult.failure(errorCode, errorMessage);

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(paymentGateway.confirm(eq(paymentKey), eq(orderId), any(Money.class)))
				.willReturn(pgResult);

			// when & then
			mockMvc.perform(post("/api/v2/payments/confirm")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("SUCCESS"))
				.andExpect(jsonPath("$.data.paymentId").isEmpty())
				.andExpect(jsonPath("$.data.success").value(false))
				.andExpect(jsonPath("$.data.errorCode").value(errorCode))
				.andExpect(jsonPath("$.data.errorMessage").value(errorMessage));

			verify(paymentRepository).findById(paymentId);
			verify(paymentGateway).confirm(eq(paymentKey), eq(orderId), any(Money.class));
			verify(confirmPaymentUseCase, never()).confirm(any(ConfirmPaymentCommand.class));
		}

		@Test
		@DisplayName("존재하지 않는 결제 ID로 요청하면 예외를 발생시킨다")
		void confirm_PaymentNotFound_ThrowsException() throws Exception {
			// given
			Long paymentId = 999L;
			String paymentKey = "toss-payment-key-123";
			String orderId = "ORDER-123456";

			PaymentConfirmRequest request = new PaymentConfirmRequest(
				paymentId,
				paymentKey,
				orderId,
				TEST_AMOUNT
			);

			given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

			// when & then
			mockMvc.perform(post("/api/v2/payments/confirm")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().is4xxClientError());

			verify(paymentRepository).findById(paymentId);
			verify(paymentGateway, never()).confirm(any(), any(), any());
			verify(confirmPaymentUseCase, never()).confirm(any());
		}

		@Test
		@DisplayName("금액이 일치하지 않으면 예외를 발생시킨다")
		void confirm_AmountMismatch_ThrowsException() throws Exception {
			// given
			Long paymentId = 1L;
			String paymentKey = "toss-payment-key-123";
			String orderId = "ORDER-123456";
			BigDecimal requestAmount = BigDecimal.valueOf(5000);  // 다른 금액

			PaymentConfirmRequest request = new PaymentConfirmRequest(
				paymentId,
				paymentKey,
				orderId,
				requestAmount
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idempotency-key")
				.orderId(orderId)
				.memberId(TEST_MEMBER_ID)
				.type(PaymentType.POINT_CHARGE)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(TEST_AMOUNT))
				.paidAmount(Money.of(TEST_AMOUNT))  // 10000원
				.orderItems(Collections.emptyList())
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

			// when & then
			mockMvc.perform(post("/api/v2/payments/confirm")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().is4xxClientError());

			verify(paymentRepository).findById(paymentId);
			verify(paymentGateway, never()).confirm(any(), any(), any());
			verify(confirmPaymentUseCase, never()).confirm(any());
		}

		@Test
		@DisplayName("다른 회원의 결제를 승인하려고 하면 예외를 발생시킨다")
		void confirm_UnauthorizedAccess_ThrowsException() throws Exception {
			// given
			Long paymentId = 1L;
			String paymentKey = "toss-payment-key-123";
			String orderId = "ORDER-123456";
			Long anotherMemberId = 200L;  // 다른 회원이 소유한 결제

			PaymentConfirmRequest request = new PaymentConfirmRequest(
				paymentId,
				paymentKey,
				orderId,
				TEST_AMOUNT
			);

			Payment payment = Payment.builder()
				.id(paymentId)
				.idempotencyKey("idempotency-key")
				.orderId(orderId)
				.memberId(anotherMemberId)  // 다른 회원 소유 (현재 사용자는 TEST_MEMBER_ID=100L)
				.type(PaymentType.POINT_CHARGE)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(TEST_AMOUNT))
				.paidAmount(Money.of(TEST_AMOUNT))
				.orderItems(Collections.emptyList())
				.status(PaymentStatus.PENDING)
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

			// when & then
			mockMvc.perform(post("/api/v2/payments/confirm")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andDo(print())
				.andExpect(status().is4xxClientError());

			verify(paymentRepository).findById(paymentId);
			verify(paymentGateway, never()).confirm(any(), any(), any());
			verify(confirmPaymentUseCase, never()).confirm(any());
		}

		@Test
		@DisplayName("필수 필드가 누락되면 400 에러를 반환한다")
		void confirm_MissingRequiredFields_ReturnsBadRequest() throws Exception {
			// given
			String requestJson = "{\"paymentKey\":\"key\",\"orderId\":\"ORDER-123\"}";

			// when & then
			mockMvc.perform(post("/api/v2/payments/confirm")
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson))
				.andDo(print())
				.andExpect(status().isBadRequest());

			verify(paymentRepository, never()).findById(anyLong());
			verify(paymentGateway, never()).confirm(any(), any(), any());
			verify(confirmPaymentUseCase, never()).confirm(any());
		}
	}
}
