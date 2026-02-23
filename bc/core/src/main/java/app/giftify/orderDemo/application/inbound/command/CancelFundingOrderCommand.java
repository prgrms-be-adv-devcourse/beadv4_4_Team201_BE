package app.giftify.orderDemo.application.inbound.command;

import app.giftify.shared.domain.vo.Money;

public record CancelFundingOrderCommand(
        Long fundingId,
        Money expiredAmount
) {
}
