package app.giftify.order.application.inbound.vo;

import app.giftify.order.domain.OrderItem;
import app.giftify.order.domain.OrderItemStatus;
import app.giftify.order.domain.type.OrderItemType;
import app.giftify.support.common.money.Money;

import java.time.LocalDateTime;

public record OrderItemDetail(
        Long orderItemId,
        Long targetId,
        OrderItemType orderItemType,
        Long sellerId,
        Long receiverId,
        Money price,
        Money amount,
        OrderItemStatus status,
        LocalDateTime cancelledAt
) {
    public static OrderItemDetail of(OrderItem orderItem) {
        return new OrderItemDetail(
                orderItem.getId(),
                orderItem.getTargetId(),
                orderItem.getOrderItemType(),
                orderItem.getSellerId(),
                orderItem.getReceiverId(),
                orderItem.getPrice(),
                orderItem.getAmount(),
                orderItem.getStatus(),
                orderItem.getCancelledAt()
        );
    }
}
