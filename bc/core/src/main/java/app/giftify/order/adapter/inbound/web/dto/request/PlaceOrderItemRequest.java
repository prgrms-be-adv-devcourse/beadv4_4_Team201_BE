package app.giftify.order.adapter.inbound.web.dto.request;

import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.vo.Money;

/**
 * 주문 항목 DTO
 * @param wishlistItemId
 * @param receiverId
 * @param amount
 * @param orderItemType
 */
public record PlaceOrderItemRequest(
        Long wishlistItemId,
        Long receiverId,
        Money amount,
        OrderItemType orderItemType
) {
}
