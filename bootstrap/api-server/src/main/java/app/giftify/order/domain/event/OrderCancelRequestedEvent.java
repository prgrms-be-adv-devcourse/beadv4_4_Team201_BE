package app.giftify.order.domain.event;

import app.giftify.support.common.event.BaseDomainEvent;
import app.giftify.support.common.money.Money;

public class OrderCancelRequestedEvent extends BaseDomainEvent {
    private final Long orderId;
    private final String orderNumber;
    private final Long paymentId;
    private final String originTransactionKey;
    private final Money cancelAmount;

    public OrderCancelRequestedEvent(Long orderId, String orderNumber, Long paymentId, String originTransactionKey, Money cancelAmount) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.paymentId = paymentId;
        this.originTransactionKey = originTransactionKey;
        this.cancelAmount = cancelAmount;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public String getOriginTransactionKey() {
        return originTransactionKey;
    }

    public Money getCancelAmount() {
        return cancelAmount;
    }
}
