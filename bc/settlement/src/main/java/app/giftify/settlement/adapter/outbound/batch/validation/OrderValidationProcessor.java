package app.giftify.settlement.adapter.outbound.batch.validation;

import app.giftify.settlement.domain.model.SettlementItem;
import app.giftify.settlement.domain.model.SettlementQueue;
import app.giftify.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

@RequiredArgsConstructor
public class OrderValidationProcessor implements ItemProcessor<SettlementItem, SettlementQueue> {

    private final ValidationAmountContext context;

    @Override
    public SettlementQueue process(SettlementItem item) {
        Long orderId = item.getOrderId();

        Money orderAmount = context.orderAmountOf(orderId);
        Money paymentAmount = context.paymentAmountOf(orderId);
        Money settlementAmount = context.settlementAmountOf(orderId);

        if (orderAmount == null || paymentAmount == null || settlementAmount == null) {
            item.manual();
        } else if (!isThreeWayMatch(orderAmount, paymentAmount, settlementAmount)) {
            item.fail();
        } else {
            item.ready();
        }

        return new SettlementQueue(item);
    }

    private static boolean isThreeWayMatch(Money orderAmount, Money paymentAmount, Money settlementAmount) {
        return orderAmount.equals(paymentAmount) && orderAmount.equals(settlementAmount);
    }
}