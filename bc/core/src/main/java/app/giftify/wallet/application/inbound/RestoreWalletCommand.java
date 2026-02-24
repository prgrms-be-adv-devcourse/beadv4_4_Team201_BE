package app.giftify.wallet.application.inbound;

import app.giftify.shared.domain.vo.Money;

public record RestoreWalletCommand(
	Long memberId,
	Money amount,
	String referenceId
) {}
