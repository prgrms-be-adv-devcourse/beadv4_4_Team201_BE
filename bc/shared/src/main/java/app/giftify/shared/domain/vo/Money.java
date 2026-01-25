package app.giftify.shared.domain.vo;

import java.math.BigDecimal;

/**
 * 금액은 수정되어선 안된다.
 * 불변 객체로 매번 새로운 객체를 반환하도록 해야 한다.
 */
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

	public static Money zero() {
		return of(0);
	}

	public static Money of(BigDecimal amount) {
		return new Money(amount);
	}

	// Money 비교용

	public boolean isLessThan(Money other) {
		return this.amount.compareTo(other.amount) < 0;
	}

	public boolean isGreaterThan(Money other) {
		return this.amount.compareTo(other.amount) > 0;
	}

	public boolean isGreaterThanOrEqual(Money other) {
		return this.amount.compareTo(other.amount) >= 0;
	}

	public Money plus(Money money) {
		if (money == null) {
			throw new IllegalArgumentException("금액은 null일 수 없습니다.");
		}

		return new Money(this.amount.add(money.amount));
	}

	public Money minus(Money money) {
		// todo: 중복 검증 코드 정리
		if (money == null) {
			throw new IllegalArgumentException("금액은 null일 수 없습니다.");
		}

		return new Money(this.amount.subtract(money.amount));
	}

}
