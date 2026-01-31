package app.giftify.orderDemo.application.inbound.command;

import app.giftify.shared.domain.type.PaymentMethodType;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;

public record PlaceOrderForItemCommand(
        Long targetId,
        TargetType targetType,

        // ParticipateInFundingCommand
        Long buyerId,
        Long receiverId,
        Money amount,
        PaymentMethodType method,

        // wishlistItem snapshot
        Long sellerId,
        Money unitPrice
) {
}