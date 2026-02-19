package app.giftify.shared.domain.event.order;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

public class OrderCancelRequestedEvent extends BaseDomainEvent {
    private final Long orderId;
    private final String orderNumber;
    private final String paymentKey;
    private final String originTransactionKey;
    private final Money cancelAmount;

    public OrderCancelRequestedEvent(Long orderId, String orderNumber, String paymentKey, String originTransactionKey, Money cancelAmount) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.paymentKey = paymentKey;
        this.originTransactionKey = originTransactionKey;
        this.cancelAmount = cancelAmount;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public String getOriginTransactionKey() {
        return originTransactionKey;
    }

    public Money getCancelAmount() {
        return cancelAmount;
    }
}
