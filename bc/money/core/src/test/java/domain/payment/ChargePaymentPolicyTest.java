package domain.payment;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

class ChargePaymentPolicyTest {

	private final ChargePaymentPolicy policy = new ChargePaymentPolicy();

	@Test
	@DisplayName("support: CHARGE 타입을 지원한다")
	void support_ShouldReturnTrue_WhenChargeType() {
		assertThat(policy.support(PaymentType.POINT_CHARGE)).isTrue();
		assertThat(policy.support(PaymentType.FUNDING)).isFalse();
	}

	@Test
	@DisplayName("validate: 충전 금액이 0원 이하이면 예외가 발생한다")
	void validate_ShouldThrowException_WhenAmountIsZeroOrLess() {
		PaymentCreateContext context = new PaymentCreateContext(1L, Money.of(0), PaymentType.POINT_CHARGE);

		assertThatThrownBy(() -> policy.validate(context))
			.isInstanceOf(PaymentException.class)
			.extracting("errorCode") // 필드명 혹은 getter 기반 추출
			.isEqualTo(PaymentErrorCode.INVALID_INPUT_VALUE);
	}
}
