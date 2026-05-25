package app.giftify.order.application.inbound.command;

import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.support.common.money.Money;

import java.util.List;

public record ParticipateFundingCommand(
        Long buyerId,
        PaymentMethod method,
        Money walletDeductAmount,
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
