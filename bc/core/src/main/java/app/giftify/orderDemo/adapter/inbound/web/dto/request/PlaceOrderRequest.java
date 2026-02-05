package app.giftify.orderDemo.adapter.inbound.web.dto.request;

import app.giftify.shared.domain.type.PaymentMethodType;

import java.util.List;

/**
 * 복수 주문 항목
 * @param items
 */
public record PlaceOrderRequest(
        List<PlaceOrderItemRequest> items,
        PaymentMethodType method
) {
}
