package domain.payment;

import app.giftify.shared.domain.event.payment.PaymentType;
import app.giftify.shared.domain.vo.Money;

public class FundingPaymentPolicy implements PaymentPolicy {
	@Override
	public boolean support(PaymentType type) {
		return type.equals(PaymentType.FUNDING);
	}

	private static final Money MIN_FUNDING_AMOUNT = Money.of(new java.math.BigDecimal("1000"));

	@Override
	public void validate(PaymentCreateContext context) {
		if (context.fundingId() == null) {
			throw new IllegalArgumentException("펀딩 결제는 펀딩 ID(fundingId)가 필수입니다.");
		}

		if (context.amount().isLessThan(MIN_FUNDING_AMOUNT)) {
			throw new IllegalArgumentException("펀딩 최소 참여 금액은 1,000원입니다.");
		}
	}
}
