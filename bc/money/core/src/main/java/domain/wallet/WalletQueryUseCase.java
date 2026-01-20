package domain.wallet;

public interface WalletQueryUseCase {
    Wallet getWallet(Long walletId);

    Wallet getWalletByMemberId(Long memberId);
}
