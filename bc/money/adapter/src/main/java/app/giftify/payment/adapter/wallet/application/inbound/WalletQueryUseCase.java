package app.giftify.payment.adapter.wallet.application.inbound;

import domain.wallet.Wallet;

public interface WalletQueryUseCase {
    Wallet getWallet(Long walletId);

    Wallet getWalletByMemberId(Long memberId);
}
