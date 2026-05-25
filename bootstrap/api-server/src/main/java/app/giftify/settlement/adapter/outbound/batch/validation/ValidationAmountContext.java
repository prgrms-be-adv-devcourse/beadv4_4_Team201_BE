package app.giftify.settlement.adapter.outbound.batch.validation;

import app.giftify.support.common.money.Money;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@StepScope
public class ValidationAmountContext {

    private Map<Long, Money> orderAmounts;
    private Map<Long, Money> paymentAmounts;
    private Map<Long, Money> settlementAmounts;

    public void loadOrderAmounts(Map<Long, Money> map) {
        this.orderAmounts = Map.copyOf(map);
    }

    public void loadPaymentAmounts(Map<Long, Money> map) {
        this.paymentAmounts = Map.copyOf(map);
    }

    public void loadSettlementAmounts(Map<Long, Money> map) {
        this.settlementAmounts = Map.copyOf(map);
    }

    public Money orderAmountOf(Long orderId) {
        return orderAmounts.get(orderId);
    }

    public Money paymentAmountOf(Long orderId) {
        return paymentAmounts.get(orderId);
    }

    public Money settlementAmountOf(Long orderId) {
        return settlementAmounts.get(orderId);
    }
}