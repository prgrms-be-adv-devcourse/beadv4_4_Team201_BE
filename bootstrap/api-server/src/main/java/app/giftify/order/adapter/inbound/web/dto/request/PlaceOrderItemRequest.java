package app.giftify.order.adapter.inbound.web.dto.request;

import app.giftify.order.domain.type.OrderItemType;
import jakarta.validation.constraints.NotNull;

/**
 * 주문 항목 DTO
 */
public record PlaceOrderItemRequest(
        @NotNull Long productId,
        Long wishlistItemId,
        Long fundingId,
        @NotNull Long receiverId,
        @NotNull Long amount,
        @NotNull OrderItemType orderItemType
) {
}
