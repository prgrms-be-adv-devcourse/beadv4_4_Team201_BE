package app.giftify.facade.command;

import app.giftify.orderDemo.adapter.inbound.web.dto.request.PlaceOrderForItemRequest;
import app.giftify.shared.domain.type.PaymentMethodType;
import app.giftify.shared.domain.vo.Money;

public record ParticipateInFundingCommand(
        Long targetId,
        Long buyerId,
        Long receiverId,
        Money amount,
        PaymentMethodType method
) {
    public static ParticipateInFundingCommand of(Long buyerId, PlaceOrderForItemRequest request) {
        return new ParticipateInFundingCommand(
                request.targetId(),
                buyerId,
                request.receiverId(),
                request.amount(),
                request.method()
        );
    }
}
