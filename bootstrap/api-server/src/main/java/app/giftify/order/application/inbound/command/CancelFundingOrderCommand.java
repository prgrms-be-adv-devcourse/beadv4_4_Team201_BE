package app.giftify.order.application.inbound.command;

import app.giftify.support.common.money.Money;

public record CancelFundingOrderCommand(
        Long fundingId,
        Money expiredAmount
) {
}
