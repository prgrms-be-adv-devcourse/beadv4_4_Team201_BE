package app.giftify.order.domain.event;

import app.giftify.support.common.event.BaseDomainEvent;
import app.giftify.order.domain.type.TargetType;
import app.giftify.support.common.money.Money;

import java.time.LocalDateTime;

public class OrderItemConfirmedEvent extends BaseDomainEvent {
    private final Long orderId;
    private final Long orderItemId;
    private final Long sellerId;
    private final Long targetId;
    private final TargetType targetType;
    private final Long paymentId;
    private final Money amount;
    private final LocalDateTime confirmedAt;

    public OrderItemConfirmedEvent(Long orderId, Long orderItemId, Long sellerId, Long targetId, TargetType targetType, Long paymentId, Money amount, LocalDateTime confirmedAt) {
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.sellerId = sellerId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.paymentId = paymentId;
        this.amount = amount;
        this.confirmedAt = confirmedAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Money getAmount() {
        return amount;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }
}
