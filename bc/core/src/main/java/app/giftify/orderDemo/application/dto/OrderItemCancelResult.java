package app.giftify.orderDemo.application.dto;

import app.giftify.shared.domain.vo.Money;

public record OrderItemCancelResult(
        Long itemId,
        Long sellerId,
        Money cancelAmount,
        OrderCancelResult result
) {
}
