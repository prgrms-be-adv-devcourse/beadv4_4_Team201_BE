package app.giftify.orderDemo.application.inbound.command;

import app.giftify.shared.domain.type.PaymentMethodType;

import java.util.List;

public record CreateOrderCommand(
        Long buyerId,
        PaymentMethodType method,
        List<CreateOrderItemCommand> items
) {
}