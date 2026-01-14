package payment.usecase.command;

import vo.Money;

public record PaymentFundingCommand(
	Long fundingId,
	Long contributorId,
	Money amount
) {
}
