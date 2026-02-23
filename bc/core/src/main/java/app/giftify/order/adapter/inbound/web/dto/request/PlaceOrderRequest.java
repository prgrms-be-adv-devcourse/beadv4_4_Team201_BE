package app.giftify.order.adapter.inbound.web.dto.request;

import app.giftify.shared.domain.type.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 복수 주문 항목
 * @param items
 */
public record PlaceOrderRequest(
        @NotNull
        @Size(min = 1)
        List<PlaceOrderItemRequest> items,
        @NotNull
        PaymentMethod method
) {
}
