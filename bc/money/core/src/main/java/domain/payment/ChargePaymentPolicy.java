package domain.payment;

import app.giftify.shared.domain.event.payment.PaymentType;
import app.giftify.shared.domain.vo.Money;

public class ChargePaymentPolicy implements PaymentPolicy {

	private static final Money MIN_CHARGE_AMOUNT = Money.of(1000); // 최소 1,000원

	@Override
	public boolean support(PaymentType type) {
		return type.equals(PaymentType.CHARGE);
	}

	@Override
	public void validate(PaymentCreateContext context) {
		if (context.amount().isLessThan(Money.of(1000))) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"충전 최소 금액은 1000원입니다.");
		}
	}
}
