package vo;

import java.math.BigDecimal;

/**
 * 금액은 수정되어선 안된다.
 * 불변 객체로 매번 새로운 객체를 반환하도록 해야 한다.
 */
public record Money(BigDecimal amount) {

	public Money {
		if (amount.signum() < 0) {
			// todo: 커스텀 예외 적용
			throw new IllegalArgumentException("금액은 음수가 될 수 없습니다.");
		}
	}

	public static Money of(long amount) {
		return new Money(BigDecimal.valueOf(amount));
	}

	public static Money zero() {
		return of(0);
	}
}
