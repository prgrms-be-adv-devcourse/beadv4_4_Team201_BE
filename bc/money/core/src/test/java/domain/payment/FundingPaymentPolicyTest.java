package domain.payment;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import app.giftify.shared.domain.event.payment.PaymentType;
import app.giftify.shared.domain.vo.Money;

class FundingPaymentPolicyTest {

	private final FundingPaymentPolicy policy = new FundingPaymentPolicy();

	@Test
	@DisplayName("support: FUNDING 타입을 지원한다")
	void support_ShouldReturnTrue_WhenFundingType() {
		assertThat(policy.support(PaymentType.FUNDING)).isTrue();
		assertThat(policy.support(PaymentType.CHARGE)).isFalse();
	}

	@Test
	@DisplayName("validate: 정상적인 펀딩 결제 요청은 통과한다")
	void validate_ShouldPass_WhenValidRequest() {
		PaymentCreateContext context = new PaymentCreateContext(1L, Money.of(10000), PaymentType.FUNDING);

		assertThatCode(() -> policy.validate(context))
			.doesNotThrowAnyException();
	}
}
