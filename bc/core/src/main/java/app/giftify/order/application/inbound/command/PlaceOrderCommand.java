package app.giftify.order.application.inbound.command;

import app.giftify.facade.command.ParticipateFundingCommand;
import app.giftify.shared.domain.type.PaymentMethod;

import java.util.List;

public record PlaceOrderCommand(
        Long buyerId,
        PaymentMethod method,
        List<PlaceOrderItemCommand> items
) {
    public static PlaceOrderCommand of(ParticipateFundingCommand command) {
        return new PlaceOrderCommand(
                command.buyerId(),
                command.method(),
                command.getPlaceOrderItemCommands()
        );
    }
}
