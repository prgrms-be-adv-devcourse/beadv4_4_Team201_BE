package app.giftify.orderDemo.adapter.inbound.web.dto.request;

import app.giftify.shared.domain.type.PaymentMethod;

import java.util.List;

/**
 * 복수 주문 항목
 * @param items
 */
public record PlaceOrderRequest(
        List<PlaceOrderItemRequest> items,
        PaymentMethod method
) {
}
