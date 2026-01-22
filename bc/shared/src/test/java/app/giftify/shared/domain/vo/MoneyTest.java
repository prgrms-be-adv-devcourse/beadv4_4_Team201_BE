package app.giftify.shared.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    @DisplayName("Money 생성 성공")
    void createMoney_Success() {
        Money money = Money.of(1000);
        assertEquals(new BigDecimal(1000), money.amount());
    }

    @Test
    @DisplayName("Money 생성 실패 - null")
    void createMoney_Fail_Null() {
        assertThrows(IllegalArgumentException.class, () -> new Money(null));
    }

    @Test
    @DisplayName("Money 생성 실패 - 음수")
    void createMoney_Fail_Negative() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(-1));
    }

    @Test
    @DisplayName("최소 금액 검증 - 성공")
    void validateMinimumAmount_Success() {
        Money money = Money.of(1000);
        assertDoesNotThrow(money::validateMinimumAmount);
    }

    @Test
    @DisplayName("최소 금액 검증 - 실패")
    void validateMinimumAmount_Fail() {
        Money money = Money.of(999);
        assertThrows(IllegalArgumentException.class, money::validateMinimumAmount);
    }

    @Test
    @DisplayName("Money 비교 - isLessThan")
    void isLessThan_Test() {
        Money money1 = Money.of(1000);
        Money money2 = Money.of(2000);
        assertTrue(money1.isLessThan(money2));
        assertFalse(money2.isLessThan(money1));
    }

    @Test
    @DisplayName("Money 비교 - isGreaterThan")
    void isGreaterThan_Test() {
        Money money1 = Money.of(2000);
        Money money2 = Money.of(1000);
        assertTrue(money1.isGreaterThan(money2));
        assertFalse(money2.isGreaterThan(money1));
    }

    @Test
    @DisplayName("Money 비교 - isGreaterThanOrEqual")
    void isGreaterThanOrEqual_Test() {
        Money money1 = Money.of(1000);
        Money money2 = Money.of(1000);
        Money money3 = Money.of(500);
        assertTrue(money1.isGreaterThanOrEqual(money2));
        assertTrue(money1.isGreaterThanOrEqual(money3));
        assertFalse(money3.isGreaterThanOrEqual(money1));
    }

    @Test
    @DisplayName("Money 더하기")
    void plus_Test() {
        Money money1 = Money.of(1000);
        Money money2 = Money.of(500);
        Money result = money1.plus(money2);
        assertEquals(Money.of(1500), result);
    }

    @Test
    @DisplayName("Money 더하기 실패 - null")
    void plus_Fail_Null() {
        Money money = Money.of(1000);
        assertThrows(IllegalArgumentException.class, () -> money.plus(null));
    }

    @Test
    @DisplayName("Money 빼기")
    void minus_Test() {
        Money money1 = Money.of(1000);
        Money money2 = Money.of(400);
        Money result = money1.minus(money2);
        assertEquals(Money.of(600), result);
    }

    @Test
    @DisplayName("Money 빼기 실패 - null")
    void minus_Fail_Null() {
        Money money = Money.of(1000);
        assertThrows(IllegalArgumentException.class, () -> money.minus(null));
    }

    @Test
    @DisplayName("Money 곱하기")
    void times_Test() {
        Money money = Money.of(1000);
        Money result = (Money) money.times(3);
        assertEquals(Money.of(3000), result);
    }

    @Test
    @DisplayName("Money zero")
    void zero_Test() {
        Money zero = Money.zero();
        assertEquals(BigDecimal.ZERO, zero.amount());
    }
}
