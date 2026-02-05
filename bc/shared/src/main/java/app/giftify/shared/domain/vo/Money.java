package app.giftify.shared.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 금액은 수정되어선 안된다.
 * 불변 객체로 매번 새로운 객체를 반환하도록 해야 한다.
 * 정산 도메인에서 차감을 처리하려면 Money 객체가 음수를 지원
 * 필요 시 Money을 사용하는 도메인에서 양수 검증 수행
 */
public record Money(BigDecimal amount) {

	public Money {
		validateNotNull(amount);
	}

	public static Money of(long amount) {
		return new Money(new BigDecimal(String.valueOf(amount)));
	}

	public static Money of(String amount) {
		return new Money(new BigDecimal(amount));
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

	public boolean isLessThanOrEqual(Money other) {
		return this.amount.compareTo(other.amount) <= 0;
	}

	public boolean isGreaterThan(Money other) {
		return this.amount.compareTo(other.amount) > 0;
	}

	public boolean isGreaterThanOrEqual(Money other) {
		return this.amount.compareTo(other.amount) >= 0;
	}

	public Money plus(Money other) {
		validateNotNull(other);

		return new Money(this.amount.add(other.amount));
	}

	public Money minus(Money other) {
		validateNotNull(other);

		return new Money(this.amount.subtract(other.amount));
	}

	public Money times(int multiplier) {
		return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)));
	}

	public Money multiply(BigDecimal multiplier) {
		return new Money(this.amount.multiply(multiplier));
	}

	public BigDecimal toBigDecimalValue() {
		return this.amount.setScale(0, RoundingMode.HALF_UP);
	}

	public Money negate() {
		return new Money(amount.negate());
	}

	private static void validateNotNull(Object money) {
		if (money == null) {
			throw new IllegalArgumentException("금액은 null일 수 없습니다.");
		}
	}

	public boolean isNegative() {
		return this.amount.compareTo(BigDecimal.ZERO) < 0;
	}

	public String toPlainString() {
		return this.amount.toPlainString();
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || getClass() != object.getClass())
			return false;

		Money money = (Money)object;
		return amount.compareTo(money.amount) == 0;
	}

	@Override
	public int hashCode() {
		return amount.stripTrailingZeros().hashCode(); // scaling 제외하고 값만 비교하기 위해
	}
}
