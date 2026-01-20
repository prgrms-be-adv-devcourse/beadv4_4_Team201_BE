package payment.usecase.command;

import app.giftify.shared.domain.vo.Money;

public record PaymentChargeCommand(
	Long userId,
	Money amount
) {
}
