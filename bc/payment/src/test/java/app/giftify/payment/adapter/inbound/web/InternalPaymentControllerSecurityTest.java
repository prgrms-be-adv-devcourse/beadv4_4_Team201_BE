package app.giftify.payment.adapter.inbound.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.HttpStatus;

import jakarta.validation.ConstraintViolationException;

import app.giftify.payment.application.inbound.InternalPaymentQueryUseCase;
import app.giftify.payment.application.inbound.InternalPaymentResult;
import app.giftify.payment.domain.PaymentMethod;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

/**
 * InternalPaymentController 보안 통합 테스트.
 *
 * <p>{@code @InternalApiOnly} 어노테이션이 올바르게 동작하는지 검증합니다.</p>
 */
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {
	InternalPaymentControllerSecurityTest.TestConfig.class,
	InternalPaymentControllerSecurityTest.TestValidationExceptionHandler.class,
	InternalPaymentController.class
})
@WebAppConfiguration
class InternalPaymentControllerSecurityTest {

	@Configuration
	@EnableWebSecurity
	@EnableMethodSecurity(prePostEnabled = true)
	@org.springframework.web.servlet.config.annotation.EnableWebMvc
	static class TestConfig {
		@Bean
		public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
			http
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
			return http.build();
		}

		@Bean
		public InternalPaymentQueryUseCase internalPaymentQueryUseCase() {
			return org.mockito.Mockito.mock(InternalPaymentQueryUseCase.class);
		}

		@Bean
		public org.springframework.http.converter.json.MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
			return new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter();
		}

		@Bean
		public org.springframework.validation.beanvalidation.MethodValidationPostProcessor methodValidationPostProcessor() {
			return new org.springframework.validation.beanvalidation.MethodValidationPostProcessor();
		}
	}

	@ControllerAdvice
	static class TestValidationExceptionHandler {
		@ExceptionHandler(ConstraintViolationException.class)
		@org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.BAD_REQUEST)
		public void handleConstraintViolation(ConstraintViolationException ex) {
			// 400 Bad Request 반환
		}
	}

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private InternalPaymentQueryUseCase internalPaymentQueryUseCase;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply(springSecurity())
			.build();
	}

	// ========== 테스트 픽스처 ========== //

	private InternalPaymentResult createTestResult() {
		return new InternalPaymentResult(
			1L,
			"order-123",
			"idempotency-key-456",
			100L,
			PaymentStatus.PAID,
			PaymentType.FUNDING,
			PaymentMethod.CARD,
			Money.of(10000),
			Money.of(10000),
			"decrypted-payment-key",
			"decrypted-approve-code"
		);
	}

	// ========== 권한 없는 요청 테스트 ========== //

	@Nested
	@DisplayName("Given 일반 사용자 권한으로 인증된 요청")
	class Given_일반_사용자_권한으로_인증된_요청 {

		@Nested
		@DisplayName("When Internal API 호출하면")
		class When_Internal_API_호출하면 {

			@Test
			@DisplayName("Then 403 Forbidden 반환")
			void Then_403_Forbidden_반환() throws Exception {
				mockMvc.perform(get("/api/internal/payments/1")
						.with(user("user").roles("USER")))
					.andExpect(status().isForbidden());
			}
		}
	}

	// ========== 입력 검증 테스트 ========== //

	@Nested
	@DisplayName("Given 유효하지 않은 Path Variable")
	class Given_유효하지_않은_Path_Variable {

		@Nested
		@DisplayName("When orderId가 공백 문자열이면")
		class When_orderId가_공백_문자열이면 {

			@Test
			@DisplayName("Then 400 Bad Request 반환")
			void Then_400_Bad_Request_반환() throws Exception {
				mockMvc.perform(get("/api/internal/payments/by-order/ ")
						.with(user("service").roles("INTERNAL_SERVICE")))
					.andExpect(status().isBadRequest());
			}
		}

		@Nested
		@DisplayName("When idempotencyKey가 공백 문자열이면")
		class When_idempotencyKey가_공백_문자열이면 {

			@Test
			@DisplayName("Then 400 Bad Request 반환")
			void Then_400_Bad_Request_반환() throws Exception {
				mockMvc.perform(get("/api/internal/payments/by-idempotency-key/ ")
						.with(user("service").roles("INTERNAL_SERVICE")))
					.andExpect(status().isBadRequest());
			}
		}
	}

	// ========== ROLE_INTERNAL_SERVICE 권한 테스트 ========== //

	@Nested
	@DisplayName("Given INTERNAL_SERVICE 역할로 인증된 요청")
	class Given_INTERNAL_SERVICE_역할로_인증된_요청 {

		@Nested
		@DisplayName("When findById API 호출하면")
		class When_findById_API_호출하면 {

			@Test
			@DisplayName("Then 200 OK와 결제 정보 반환")
			void Then_200_OK와_결제_정보_반환() throws Exception {
				// given
				given(internalPaymentQueryUseCase.findById(1L))
					.willReturn(Optional.of(createTestResult()));

				// when & then
				mockMvc.perform(get("/api/internal/payments/1")
						.with(user("service").roles("INTERNAL_SERVICE")))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.paymentId").value(1L))
					.andExpect(jsonPath("$.paymentKey").value("decrypted-payment-key"))
					.andExpect(jsonPath("$.approveCode").value("decrypted-approve-code"));
			}

			@Test
			@DisplayName("Then 존재하지 않으면 404 Not Found")
			void Then_존재하지_않으면_404_Not_Found() throws Exception {
				// given
				given(internalPaymentQueryUseCase.findById(999L))
					.willReturn(Optional.empty());

				// when & then
				mockMvc.perform(get("/api/internal/payments/999")
						.with(user("service").roles("INTERNAL_SERVICE")))
					.andExpect(status().isNotFound());
			}
		}

		@Nested
		@DisplayName("When findByOrderId API 호출하면")
		class When_findByOrderId_API_호출하면 {

			@Test
			@DisplayName("Then 200 OK와 결제 목록 반환")
			void Then_200_OK와_결제_목록_반환() throws Exception {
				// given
				given(internalPaymentQueryUseCase.findByOrderId("order-123"))
					.willReturn(List.of(createTestResult()));

				// when & then
				mockMvc.perform(get("/api/internal/payments/by-order/order-123")
						.with(user("service").roles("INTERNAL_SERVICE")))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[0].paymentId").value(1L))
					.andExpect(jsonPath("$[0].orderId").value("order-123"));
			}
		}

		@Nested
		@DisplayName("When findByIdempotencyKey API 호출하면")
		class When_findByIdempotencyKey_API_호출하면 {

			@Test
			@DisplayName("Then 200 OK와 결제 정보 반환")
			void Then_200_OK와_결제_정보_반환() throws Exception {
				// given
				given(internalPaymentQueryUseCase.findByIdempotencyKey("idempotency-key-456"))
					.willReturn(Optional.of(createTestResult()));

				// when & then
				mockMvc.perform(get("/api/internal/payments/by-idempotency-key/idempotency-key-456")
						.with(user("service").roles("INTERNAL_SERVICE")))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.idempotencyKey").value("idempotency-key-456"));
			}
		}
	}
}
