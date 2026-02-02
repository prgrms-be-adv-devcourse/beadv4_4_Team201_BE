package app.giftify.orderDemo.application.inbound.command;

import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;

public record CreateOrderItemCommand (
        Long targetId,
        Long receiverId,
        Money amount,
        OrderItemType orderItemType,

        // funding 유무
        TargetType targetType,

        // WishlistItemSnapshot
        Long sellerId,
        Money price
){
}

