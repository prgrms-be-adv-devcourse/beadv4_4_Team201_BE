package app.giftify.order.application.dto;

import app.giftify.order.domain.ResultCode;
import app.giftify.shared.domain.vo.Money;

public record OrderItemCancelResult(
        Long itemId,
        Long sellerId,
        Money cancelAmount,
        ResultCode result
) {
}
