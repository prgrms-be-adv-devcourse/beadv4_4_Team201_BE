package app.giftify.order.application.inbound.command;

import app.giftify.facade.command.PlaceOrderCommand;
import app.giftify.order.adapter.inbound.web.dto.request.PlaceOrderItemRequest;
import app.giftify.shared.domain.type.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderCommand(
        @NotNull
        Long buyerId,
        @NotNull
        PaymentMethod method,
        @NotNull
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
