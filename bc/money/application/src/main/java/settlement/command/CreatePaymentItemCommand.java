package settlement.command;

import app.giftify.shared.domain.vo.OrderItemInfo;

import java.util.Objects;

public record CreatePaymentItemCommand(
        Long sellerId,
        OrderItemInfo orderItemInfo
) {
    public CreatePaymentItemCommand {
        Objects.requireNonNull(sellerId, "판매자 ID는 필수입니다.");
        Objects.requireNonNull(orderItemInfo, "주문 정보는 필수입니다.");
    }
}