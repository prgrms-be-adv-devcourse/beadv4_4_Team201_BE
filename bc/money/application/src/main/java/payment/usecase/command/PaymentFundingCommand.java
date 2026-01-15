package payment.usecase.command;

import app.giftify.shared.domain.vo.Money;

public record PaymentFundingCommand(
        Long fundingId,
        Long contributorId,
        Money amount
) {
}
