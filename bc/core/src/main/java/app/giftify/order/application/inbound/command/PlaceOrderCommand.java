package app.giftify.order.application.inbound.command;

import app.giftify.facade.command.ParticipateFundingCommand;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.PaymentMethod;

import java.util.EnumSet;
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

    public List<Long> getProductIds() {
        return items.stream()
                .map(PlaceOrderItemCommand::productId)
                .toList();
    }

    public List<Long> getWishlistItemIdsByOrderItemType(EnumSet<OrderItemType> types) {
        return items.stream()
                .filter(item -> types.contains(item.orderItemType()))
                .map(PlaceOrderItemCommand::wishlistItemId)
                .toList();
    }

    public boolean isAllNormalOrder() {
        return items.stream()
                .allMatch(item -> item.orderItemType() == OrderItemType.NORMAL_ORDER);
    }
}
