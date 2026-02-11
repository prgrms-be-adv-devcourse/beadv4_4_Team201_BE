package app.giftify.orderDemo.adapter.inbound.web.dto.request;

import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.support.common.annotation.Amount;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

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
        @Override
        public boolean equals(Object o) {
                if (o == null || getClass() != o.getClass()) return false;
                PlaceOrderItemRequest that = (PlaceOrderItemRequest) o;
                return Objects.equals(amount(), that.amount()) && Objects.equals(receiverId(), that.receiverId()) && Objects.equals(wishlistItemId(), that.wishlistItemId()) && orderItemType() == that.orderItemType();
        }

        @Override
        public int hashCode() {
                return Objects.hash(wishlistItemId(), receiverId(), amount(), orderItemType());
        }
}
