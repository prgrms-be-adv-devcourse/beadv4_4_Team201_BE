package app.giftify.order.application.inbound.command;

import app.giftify.facade.command.ParticipateFundingCommand;
import app.giftify.order.adapter.inbound.web.dto.request.PlaceOrderItemRequest;
import app.giftify.shared.domain.type.PaymentMethod;

import java.util.List;

public record PlaceOrderCommand(
        Long buyerId,
        PaymentMethod method,
        List<PlaceOrderItemRequest> itemRequests
) {
    public static PlaceOrderCommand of(ParticipateFundingCommand command) {
        return new PlaceOrderCommand(
                command.buyerId(),
                command.method(),
                command.items()
        );
    }
}
