package app.giftify.payment.adapter.in.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
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

import app.giftify.payment.adapter.in.web.exception.MoneyGlobalExceptionHandler;
import app.giftify.payment.adapter.in.web.payment.PaymentController;
import app.giftify.payment.adapter.in.web.payment.dto.PaymentChargeRequest;
import app.giftify.payment.adapter.in.web.payment.dto.PaymentConfirmRequest;
import app.giftify.payment.adapter.in.web.payment.dto.PaymentInitiateRequest;
import app.giftify.shared.domain.event.payment.PaymentType;
import app.giftify.payment.adapter.out.pg.TossConfirmResult;
import app.giftify.payment.adapter.out.pg.TossPaymentsClient;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.domain.vo.Money;
import domain.payment.Payment;
import domain.payment.PaymentRepository;
import domain.payment.PaymentStatus;
import payment.usecase.PaymentChargeUseCase;
import payment.usecase.PaymentCompleteUseCase;
import payment.usecase.PaymentInitiateUseCase;
import payment.usecase.result.PaymentInitiateResult;
import payment.usecase.result.PaymentResult;

/**
 * PaymentController 테스트.
 *
 * <p>standaloneSetup을 사용하여 @CurrentMemberId 어노테이션 주입을 테스트</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentController 테스트")
class PaymentControllerTest {

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@Mock
	private PaymentChargeUseCase paymentChargeUseCase;

	@Mock
	private PaymentCompleteUseCase paymentCompleteUseCase;

	@Mock
	private PaymentInitiateUseCase paymentInitiateUseCase;

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private TossPaymentsClient tossPaymentsClient;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
	}

	@Nested
	@DisplayName("POST /api/payments/charge")
	class ChargeTest {

		@Test
		@DisplayName("충전 요청 성공 시 PENDING 상태의 Payment 정보를 반환한다")
		void charge_ShouldReturnPendingPayment_WhenValidRequest() throws Exception {
			// Given
			Long memberId = 100L;
			mockMvc = createMockMvc(memberId);

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
			mockMvc = createMockMvc(100L);
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
			Long memberId = 100L;
			mockMvc = createMockMvc(memberId);

			Long paymentId = 1L;
			String orderUuid = "GFTFY_CHARGE_test123uuid";
			String paymentKey = "toss_payment_key_xxx";
			BigDecimal amount = new BigDecimal("10000");

			PaymentConfirmRequest request = new PaymentConfirmRequest(paymentKey, paymentId, amount);

			Payment payment = Payment.builder()
				.paymentId(paymentId)
				.orderUuid(orderUuid)
				.userId(memberId) // memberId와 일치
				.status(PaymentStatus.PENDING)
				.amount(Money.of(amount))
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(tossPaymentsClient.confirm(eq(paymentKey), eq(orderUuid), eq(amount)))
				.willReturn(TossConfirmResult.success(paymentKey));

			// When & Then
			mockMvc.perform(post("/api/payments/confirm")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
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
			mockMvc = createMockMvc(100L);
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
			Long memberId = 100L;
			mockMvc = createMockMvc(memberId);

			Long paymentId = 1L;
			PaymentConfirmRequest request = new PaymentConfirmRequest(
				"paymentKey", paymentId, new BigDecimal("20000") // 다른 금액
			);

			Payment payment = Payment.builder()
				.paymentId(paymentId)
				.orderUuid("GFTFY_CHARGE_test")
				.userId(memberId)
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
			Long memberId = 100L;
			mockMvc = createMockMvc(memberId);

			Long paymentId = 1L;
			String orderId = "GFTFY_CHARGE_test";
			String paymentKey = "paymentKey";
			BigDecimal amount = new BigDecimal("10000");

			PaymentConfirmRequest request = new PaymentConfirmRequest(paymentKey, paymentId, amount);

			Payment payment = Payment.builder()
				.paymentId(paymentId)
				.orderUuid(orderId)
				.userId(memberId)
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

		@Test
		@DisplayName("다른 사용자의 결제 정보 접근 시 400 Bad Request를 반환한다")
		void confirm_ShouldReturnBadRequest_WhenAccessingOthersPayment() throws Exception {
			// Given
			Long requestingMemberId = 100L;
			Long actualOwnerId = 999L;
			mockMvc = createMockMvc(requestingMemberId);

			Long paymentId = 1L;
			PaymentConfirmRequest request = new PaymentConfirmRequest(
				"paymentKey", paymentId, new BigDecimal("10000")
			);

			Payment payment = Payment.builder()
				.paymentId(paymentId)
				.orderUuid("GFTFY_CHARGE_test")
				.userId(actualOwnerId) // 다른 사용자의 결제
				.status(PaymentStatus.PENDING)
				.amount(Money.of(new BigDecimal("10000")))
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

			// When & Then
			mockMvc.perform(post("/api/payments/confirm")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("POST /api/payments/initiate")
	class InitiateTest {

		@Test
		@DisplayName("예치금으로 완납 시 completed=true를 반환한다")
		void initiate_ShouldReturnCompleted_WhenWalletSufficient() throws Exception {
			// Given
			Long memberId = 100L;
			Long orderId = 100L;
			Long paymentId = 999L;
			mockMvc = createMockMvc(memberId);

			BigDecimal amount = new BigDecimal("5000");
			PaymentInitiateRequest request = new PaymentInitiateRequest(orderId, amount);

			PaymentInitiateResult result = PaymentInitiateResult.completedWithWallet(orderId, Money.of(amount), paymentId);
			given(paymentInitiateUseCase.initiate(any())).willReturn(result);

			// When & Then
			mockMvc.perform(post("/api/payments/initiate")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("SUCCESS"))
				.andExpect(jsonPath("$.data.completed").value(true))
				.andExpect(jsonPath("$.data.orderId").value(100))
				.andExpect(jsonPath("$.data.walletUsed").value(5000))
				.andExpect(jsonPath("$.data.pgPaymentRequired").value(0))
				.andExpect(jsonPath("$.data.paymentId").value(999))
				.andExpect(jsonPath("$.data.pgOrderId").isEmpty());
		}

		@Test
		@DisplayName("복합 결제 시 completed=false와 pgOrderId를 반환한다 (향후 확장용)")
		void initiate_ShouldReturnPgOrderId_WhenPgPaymentRequired() throws Exception {
			// Given
			Long memberId = 100L;
			Long orderId = 100L;
			mockMvc = createMockMvc(memberId);

			BigDecimal amount = new BigDecimal("50000");
			PaymentInitiateRequest request = new PaymentInitiateRequest(orderId, amount);

			PaymentInitiateResult result = PaymentInitiateResult.requiresPgPayment(
				orderId, Money.of(30000), Money.of(20000), 1L, "GFTFY_PAYMENT_test123"
			);
			given(paymentInitiateUseCase.initiate(any())).willReturn(result);

			// When & Then
			mockMvc.perform(post("/api/payments/initiate")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("SUCCESS"))
				.andExpect(jsonPath("$.data.completed").value(false))
				.andExpect(jsonPath("$.data.orderId").value(100))
				.andExpect(jsonPath("$.data.walletUsed").value(30000))
				.andExpect(jsonPath("$.data.pgPaymentRequired").value(20000))
				.andExpect(jsonPath("$.data.paymentId").value(1))
				.andExpect(jsonPath("$.data.pgOrderId").value("GFTFY_PAYMENT_test123"))
				.andExpect(jsonPath("$.data.orderName").value("Giftify 결제"));
		}

		@Test
		@DisplayName("최소 금액 미만 요청 시 400 Bad Request를 반환한다")
		void initiate_ShouldReturnBadRequest_WhenAmountBelowMinimum() throws Exception {
			// Given
			mockMvc = createMockMvc(100L);
			PaymentInitiateRequest request = new PaymentInitiateRequest(100L, new BigDecimal("500"));

			// When & Then
			mockMvc.perform(post("/api/payments/initiate")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("POST /api/payments/confirm - FUNDING 롤백")
	class ConfirmFundingRollbackTest {

		@Test
		@DisplayName("FUNDING PG 결제 실패 시 예치금이 롤백된다")
		void confirm_ShouldRollbackWallet_WhenFundingPgFails() throws Exception {
			// Given
			Long memberId = 100L;
			mockMvc = createMockMvc(memberId);

			Long paymentId = 1L;
			String orderUuid = "GFTFY_FUNDING_test123";
			String paymentKey = "toss_key";
			BigDecimal pgAmount = new BigDecimal("20000");
			Money walletUsed = Money.of(30000);

			PaymentConfirmRequest request = new PaymentConfirmRequest(paymentKey, paymentId, pgAmount);

			// FUNDING 타입 + walletUsedAmount 있는 Payment
			Payment payment = Payment.builder()
				.paymentId(paymentId)
				.orderUuid(orderUuid)
				.userId(memberId)
				.type(PaymentType.FUNDING)
				.status(PaymentStatus.PENDING)
				.amount(Money.of(pgAmount))
				.walletUsedAmount(walletUsed)
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(tossPaymentsClient.confirm(eq(paymentKey), eq(orderUuid), eq(pgAmount)))
				.willReturn(TossConfirmResult.failure("CARD_ERROR", "카드 오류"));

			// When & Then
			mockMvc.perform(post("/api/payments/confirm")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());

			// 롤백 검증
			verify(paymentInitiateUseCase).rollbackWallet(memberId, walletUsed, paymentId);
			verify(paymentCompleteUseCase).complete(paymentId, paymentKey, false);
		}

		@Test
		@DisplayName("CHARGE 타입 PG 결제 실패 시 롤백하지 않는다")
		void confirm_ShouldNotRollback_WhenChargeTypeFails() throws Exception {
			// Given
			Long memberId = 100L;
			mockMvc = createMockMvc(memberId);

			Long paymentId = 1L;
			String orderUuid = "GFTFY_CHARGE_test";
			String paymentKey = "toss_key";
			BigDecimal amount = new BigDecimal("10000");

			PaymentConfirmRequest request = new PaymentConfirmRequest(paymentKey, paymentId, amount);

			// CHARGE 타입 Payment
			Payment payment = Payment.builder()
				.paymentId(paymentId)
				.orderUuid(orderUuid)
				.userId(memberId)
				.type(PaymentType.CHARGE)
				.status(PaymentStatus.PENDING)
				.amount(Money.of(amount))
				.build();

			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(tossPaymentsClient.confirm(eq(paymentKey), eq(orderUuid), eq(amount)))
				.willReturn(TossConfirmResult.failure("CARD_ERROR", "카드 오류"));

			// When & Then
			mockMvc.perform(post("/api/payments/confirm")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());

			// 롤백 호출 없음
			verify(paymentInitiateUseCase, never()).rollbackWallet(anyLong(), any(), anyLong());
		}
	}

	private MockMvc createMockMvc(Long memberId) {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(objectMapper);

		PaymentController controller = new PaymentController(
			paymentChargeUseCase,
			paymentCompleteUseCase,
			paymentInitiateUseCase,
			paymentRepository,
			tossPaymentsClient
		);

		return MockMvcBuilders
			.standaloneSetup(controller) // 테스트용 셋업
			.setMessageConverters(converter)
			.setControllerAdvice(new MoneyGlobalExceptionHandler())
			.setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
				@Override
				public boolean supportsParameter(MethodParameter parameter) {
					return parameter.hasParameterAnnotation(CurrentMemberId.class);
				}

				@Override
				public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
					NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
					return memberId;
				}
			})
			.build();
	}
}
