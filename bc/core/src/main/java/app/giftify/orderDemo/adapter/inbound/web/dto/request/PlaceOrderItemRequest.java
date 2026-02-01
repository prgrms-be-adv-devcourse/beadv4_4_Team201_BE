package app.giftify.orderDemo.adapter.inbound.web.dto.request;

import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.vo.Money;

/**
 * 주문 항목 DTO
 * @param targetId
 * @param receiverId
 * @param amount
 * @param orderItemType
 */
public record PlaceOrderItemRequest(
        Long targetId,
        Long receiverId,
        Money amount,
        OrderItemType orderItemType
) {
}
