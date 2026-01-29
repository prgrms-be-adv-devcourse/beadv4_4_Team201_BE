package app.giftify.settlement.application.inbound;

import app.giftify.settlement.domain.OrderItemInfo;

import java.util.Objects;

public record CreatePaymentItemCommand(OrderItemInfo orderItemInfo
) {
    public CreatePaymentItemCommand {
        Objects.requireNonNull(orderItemInfo, "주문 정보는 필수입니다.");
    }
}