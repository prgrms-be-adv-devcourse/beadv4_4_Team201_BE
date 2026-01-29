package app.giftify.order.domain.vo;

import java.math.BigDecimal;

public record Money(BigDecimal amount) {

	public Money {
		if (amount == null) {
			throw new IllegalArgumentException("금액은 null일 수 없습니다.");
		}

		if (amount.signum() < 0) {
			throw new IllegalArgumentException("금액은 음수가 될 수 없습니다.");
		}

		if (amount.signum() != 0 && amount.compareTo(BigDecimal.valueOf(1000)) < 0) {
			throw new IllegalArgumentException("금액은 1000원 이상이어야 합니다.");
		}
	}

	public static Money of(long amount) {
		return new Money(BigDecimal.valueOf(amount));
	}

	public static Money of(BigDecimal amount) {
		return new Money(amount);
	}

	public Money plus(Money money) {
		if (money == null) {
			throw new IllegalArgumentException("금액은 null일 수 없습니다.");
		}

		return new Money(this.amount.add(money.amount));
	}

	public static Money zero() {
		return of(0);
	}
}
