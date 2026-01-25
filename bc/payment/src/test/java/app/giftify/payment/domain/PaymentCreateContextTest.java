package app.giftify.payment.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import app.giftify.shared.domain.type.PaymentType;

/**
 * PaymentCreateContext 검증 테스트.
 * Compact Constructor의 필수 필드 검증을 테스트합니다.
 */
class PaymentCreateContextTest {

	// ========== 실패 케이스 (우선) ========== //

	@Nested
	@DisplayName("Given 필수 필드 누락 시")
	class Given_필수_필드_누락_시 {

		@Test
		@DisplayName("memberId가 null이면 PaymentException 발생")
		void memberId_null() {
			assertThatThrownBy(() ->
				new PaymentCreateContext(null, "order-123", PaymentType.FUNDING, PaymentMethod.WALLET))
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("memberId는 필수");
		}

		@Test
		@DisplayName("orderId가 null이면 PaymentException 발생")
		void orderId_null() {
			assertThatThrownBy(() ->
				new PaymentCreateContext(1L, null, PaymentType.FUNDING, PaymentMethod.WALLET))
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("orderId는 필수");
		}

		@Test
		@DisplayName("orderId가 빈 문자열이면 PaymentException 발생")
		void orderId_blank() {
			assertThatThrownBy(() ->
				new PaymentCreateContext(1L, "   ", PaymentType.FUNDING, PaymentMethod.WALLET))
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("orderId는 필수");
		}

		@Test
		@DisplayName("type이 null이면 PaymentException 발생")
		void type_null() {
			assertThatThrownBy(() ->
				new PaymentCreateContext(1L, "order-123", null, PaymentMethod.WALLET))
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("type은 필수");
		}

		@Test
		@DisplayName("method가 null이면 PaymentException 발생")
		void method_null() {
			assertThatThrownBy(() ->
				new PaymentCreateContext(1L, "order-123", PaymentType.FUNDING, null))
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("method는 필수");
		}
	}

	// ========== 성공 케이스 ========== //

	@Nested
	@DisplayName("Given 모든 필수 필드가 유효한 경우")
	class Given_모든_필수_필드가_유효한_경우 {

		@Test
		@DisplayName("PaymentCreateContext 생성 성공")
		void 생성_성공() {
			assertThatCode(() ->
				new PaymentCreateContext(1L, "order-123", PaymentType.FUNDING, PaymentMethod.WALLET))
				.doesNotThrowAnyException();
		}
	}
}