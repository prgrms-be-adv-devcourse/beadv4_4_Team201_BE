package app.giftify.orderDemo.adapter.inbound.web.dto.request;

import app.giftify.shared.domain.type.PaymentMethodType;
import app.giftify.shared.domain.vo.Money;

public record PlaceOrderForItemRequest(
        Long targetId,
        Long receiverId,
        Money amount,
        PaymentMethodType method
) {
}
