package app.giftify.order.application.dto;

import app.giftify.order.domain.ResultCode;
import app.giftify.support.common.money.Money;

public record OrderItemCancelResult(
        Long itemId,
        Long sellerId,
        Money cancelAmount,
        ResultCode result
) {
}
