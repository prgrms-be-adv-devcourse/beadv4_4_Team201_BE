package app.giftify.facade.command;

import app.giftify.order.adapter.inbound.web.dto.request.PlaceOrderItemRequest;
import app.giftify.order.adapter.inbound.web.dto.request.PlaceOrderRequest;
import app.giftify.shared.domain.type.PaymentMethod;

import java.util.List;

public record ParticipateFundingCommand(
        Long buyerId,
        PaymentMethod method,
        List<PlaceOrderItemRequest> items
) {
    public static ParticipateFundingCommand of(Long buyerId, PlaceOrderRequest request) {
        return new ParticipateFundingCommand(
                buyerId,
                request.method(),
                request.items()
        );
    }
}
