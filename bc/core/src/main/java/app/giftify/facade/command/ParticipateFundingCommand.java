package app.giftify.facade.command;

import app.giftify.order.application.inbound.command.PlaceOrderItemCommand;
import app.giftify.shared.domain.type.PaymentMethod;

import java.util.List;

public record ParticipateFundingCommand(
        Long buyerId,
        PaymentMethod method,
        List<ParticipateFundingItemCommand> items
) {
    public List<PlaceOrderItemCommand> getPlaceOrderItemCommands() {
        return items().stream()
                .map(item -> new PlaceOrderItemCommand(
                        item.productId(),
                        item.wishlistItemId(),
                        item.fundingId(),
                        item.receiverId(),
                        item.amount(),
                        item.orderItemType()
                ))
                .toList();
    }
}
