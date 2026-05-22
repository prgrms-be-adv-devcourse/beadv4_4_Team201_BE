package app.giftify.settlement.domain.model;

import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettlementCoreTest {

    @Test
    @DisplayName("정상 금액으로 생성에 성공한다")
    void create_withValidAmounts_succeeds() {
        // given
        Money paidAmount = Money.of(10000);
        Money platformFee = Money.of(100);
        Money pgFee = Money.of(200);
        Money settlementAmount = Money.of(9700);

        // when
        SettlementCore core = new SettlementCore(paidAmount, platformFee, pgFee, settlementAmount);

        // then
        assertThat(core.paidAmount()).isEqualTo(paidAmount);
        assertThat(core.platformFee()).isEqualTo(platformFee);
        assertThat(core.pgFee()).isEqualTo(pgFee);
        assertThat(core.settlementAmount()).isEqualTo(settlementAmount);
    }

    @Test
    @DisplayName("금액이 null이면 예외가 발생한다")
    void create_withNullAmount_throwsException() {
        // given / when / then
        assertThatThrownBy(() -> new SettlementCore(null, Money.of(100), Money.of(200), Money.of(9700)))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("음수 금액이면 예외가 발생한다")
    void create_withNegativeAmount_throwsException() {
        // given / when / then
        assertThatThrownBy(() -> new SettlementCore(Money.of(-10000), Money.of(100), Money.of(200), Money.of(9700)))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("paid - platformFee - pgFee ≠ settlementAmount이면 예외가 발생한다")
    void create_withMismatchedSettlementAmount_throwsException() {
        // given / when / then
        assertThatThrownBy(() -> new SettlementCore(Money.of(10000), Money.of(100), Money.of(200), Money.of(5000)))
                .isInstanceOf(DomainException.class);
    }
}
