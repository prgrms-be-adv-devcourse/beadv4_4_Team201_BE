package app.giftify.orderDemo.adapter.inbound.web.dto.request;

import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.support.common.annotation.Amount;
import jakarta.validation.constraints.NotNull;

/**
 * 주문 항목 DTO
 * @param wishlistItemId
 * @param receiverId
 * @param amount
 * @param orderItemType
 */
public record PlaceOrderItemRequest(
        @NotNull
        Long wishlistItemId,
        @NotNull
        Long receiverId,
        @NotNull
        @Amount
        Money amount,
        @NotNull
        OrderItemType orderItemType
) {
}
