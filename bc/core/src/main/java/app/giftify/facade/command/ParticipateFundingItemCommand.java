package app.giftify.facade.command;

import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.vo.Money;

public record ParticipateFundingItemCommand(
        Long productId,
        Long wishlistItemId,
        Long fundingId,
        Long receiverId,
        Money amount,
        OrderItemType orderItemType
) {
}
