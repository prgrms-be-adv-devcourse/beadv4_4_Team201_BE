package app.giftify.settlement.domain.model;

import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementAmountSummaryTest {

    @Test
    @DisplayName("여러 큐의 금액을 올바르게 합산한다")
    void of_withMultipleQueues_aggregatesCorrectly() {
        // given
        SettlementCore core1 = new SettlementCore(
                Money.of(10000), Money.of(100), Money.of(200), Money.of(9700)
        );
        SettlementCore core2 = new SettlementCore(
                Money.of(20000), Money.of(200), Money.of(400), Money.of(19400)
        );

        SettlementItem item1 = mock(SettlementItem.class);
        SettlementItem item2 = mock(SettlementItem.class);
        when(item1.getCore()).thenReturn(core1);
        when(item2.getCore()).thenReturn(core2);

        SettlementQueue queue1 = mock(SettlementQueue.class);
        SettlementQueue queue2 = mock(SettlementQueue.class);
        when(queue1.getItem()).thenReturn(item1);
        when(queue2.getItem()).thenReturn(item2);

        // when
        SettlementAmountSummary summary = SettlementAmountSummary.of(List.of(queue1, queue2));

        // then
        assertThat(summary.salesAmount()).isEqualTo(Money.of(30000));
        assertThat(summary.platformFee()).isEqualTo(Money.of(300));
        assertThat(summary.pgFee()).isEqualTo(Money.of(600));
        assertThat(summary.settlementAmount()).isEqualTo(Money.of(29100));
    }

    @Test
    @DisplayName("금액 불변식이 위반되면 예외가 발생한다")
    void create_withMismatchedAmount_throwsException() {
        // given / when / then
        assertThatThrownBy(() -> new SettlementAmountSummary(
                Money.of(10000), Money.of(100), Money.of(200), Money.of(5000)
        )).isInstanceOf(DomainException.class);
    }
}
