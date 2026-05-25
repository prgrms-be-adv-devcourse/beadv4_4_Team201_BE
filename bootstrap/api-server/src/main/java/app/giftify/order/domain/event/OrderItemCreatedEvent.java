package app.giftify.order.domain.event;

import app.giftify.support.common.event.BaseDomainEvent;
import app.giftify.order.domain.type.OrderItemType;
import app.giftify.order.domain.type.TargetType;
import app.giftify.support.common.money.Money;

/**
 * targetType == FUNDING 일 때만 발행
 */
public class OrderItemCreatedEvent extends BaseDomainEvent {
    private final Long orderItemId;
    private final Long targetId;
    private final TargetType targetType;
    private final OrderItemType orderItemType;
    private final Long orderId;
    private final Long sellerId;
    private final Money price;
    private final Money amount;

    public OrderItemCreatedEvent(Long orderItemId, Long targetId, TargetType targetType, OrderItemType orderItemType, Long orderId, Long sellerId, Money price, Money amount) {
        this.orderItemId = orderItemId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.orderItemType = orderItemType;
        this.orderId = orderId;
        this.sellerId = sellerId;
        this.price = price;
        this.amount = amount;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public OrderItemType getOrderItemType() {
        return orderItemType;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public Money getPrice() {
        return price;
    }

    public Money getAmount() {
        return amount;
    }
}
