package app.giftify.order.application.inbound.command;

import app.giftify.order.domain.type.OrderItemType;
import app.giftify.support.common.money.Money;

public record ParticipateFundingItemCommand(
        Long productId,
        Long wishlistItemId,
        Long fundingId,
        Long receiverId,
        Money amount,
        OrderItemType orderItemType
) {
}
