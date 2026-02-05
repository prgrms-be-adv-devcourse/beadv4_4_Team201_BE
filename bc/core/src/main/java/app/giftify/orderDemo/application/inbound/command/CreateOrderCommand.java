package app.giftify.orderDemo.application.inbound.command;

import app.giftify.shared.domain.type.PaymentMethod;

import java.util.List;

public record CreateOrderCommand(
        Long buyerId,
        PaymentMethod method,
        List<CreateOrderItemCommand> items
) {
}