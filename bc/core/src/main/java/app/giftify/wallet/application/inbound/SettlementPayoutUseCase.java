package app.giftify.wallet.application.inbound;

public interface SettlementPayoutUseCase {
	void payout(SettlementPayoutCommand command);
}
