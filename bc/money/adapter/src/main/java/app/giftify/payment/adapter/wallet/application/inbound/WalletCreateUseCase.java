package app.giftify.payment.adapter.wallet.application.inbound;

import domain.wallet.Wallet;

public interface WalletCreateUseCase {

    Wallet createWallet(Long memberId);
}
