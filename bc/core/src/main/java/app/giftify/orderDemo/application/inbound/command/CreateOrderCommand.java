package app.giftify.orderDemo.application.inbound.command;

import app.giftify.facade.command.PlaceOrderCommand;
import app.giftify.orderDemo.adapter.inbound.web.dto.request.PlaceOrderItemRequest;
import app.giftify.shared.domain.type.PaymentMethod;

import java.util.List;

public record CreateOrderCommand(
        Long buyerId,
        PaymentMethod method,
        List<PlaceOrderItemRequest> itemRequests
) {
    public static CreateOrderCommand of(PlaceOrderCommand command) {
        return new CreateOrderCommand(
                command.buyerId(),
                command.method(),
                command.items()
        );
    }
}