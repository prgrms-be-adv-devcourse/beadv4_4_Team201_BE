package app.giftify.settlement.adapter.outbound.batch.validation;

import app.giftify.settlement.domain.model.SettlementItem;
import app.giftify.settlement.domain.model.SettlementQueue;
import app.giftify.settlement.domain.status.SettlementItemStatus;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderValidationProcessorTest {

    @Mock
    private ValidationAmountContext context;

    @Mock
    private SettlementItem item;

    @InjectMocks
    private OrderValidationProcessor processor;

    @Test
    @DisplayName("3-way match 성공 시 SettlementQueue를 반환하고 item이 READY 상태가 된다")
    void process_allAmountsMatch_returnsQueueWithReadyItem() {
        // given
        Long orderId = 1L;
        Money amount = Money.of(10000);

        when(item.getOrderId()).thenReturn(orderId);
        when(context.orderAmountOf(orderId)).thenReturn(amount);
        when(context.paymentAmountOf(orderId)).thenReturn(amount);
        when(context.settlementAmountOf(orderId)).thenReturn(amount);

        // when
        SettlementQueue result = processor.process(item);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getItem()).isEqualTo(item);
        verify(item).validating();
        verify(item).ready();
    }

    @Test
    @DisplayName("금액이 null이면 null을 반환하고 item이 MANUAL 상태가 된다")
    void process_anyAmountNull_returnsNullWithManualItem() {
        // given
        Long orderId = 1L;

        when(item.getOrderId()).thenReturn(orderId);
        when(context.orderAmountOf(orderId)).thenReturn(null);
        when(context.paymentAmountOf(orderId)).thenReturn(Money.of(10000));
        when(context.settlementAmountOf(orderId)).thenReturn(Money.of(10000));

        // when
        SettlementQueue result = processor.process(item);

        // then
        assertThat(result).isNull();
        verify(item).validating();
        verify(item).manual();
    }

    @Test
    @DisplayName("금액 불일치 시 null을 반환하고 item이 VALIDATE_FAILED 상태가 된다")
    void process_amountsMismatch_returnsNullWithValidateFailedItem() {
        // given
        Long orderId = 1L;

        when(item.getOrderId()).thenReturn(orderId);
        when(context.orderAmountOf(orderId)).thenReturn(Money.of(10000));
        when(context.paymentAmountOf(orderId)).thenReturn(Money.of(9000));
        when(context.settlementAmountOf(orderId)).thenReturn(Money.of(10000));

        // when
        SettlementQueue result = processor.process(item);

        // then
        assertThat(result).isNull();
        verify(item).validating();
        verify(item).failToValidate();
    }
}
