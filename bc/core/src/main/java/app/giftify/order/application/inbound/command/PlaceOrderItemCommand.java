package app.giftify.order.application.inbound.command;

import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.vo.Money;

public record PlaceOrderItemCommand(
        Long productId,
        Long wishlistItemId,
        Long fundingId,
        Long receiverId,
        Money amount,
        OrderItemType orderItemType
) {

}
