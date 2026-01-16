package wallet.service;

import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.wallet.WalletChargeCompletedEvent;
import app.giftify.shared.domain.vo.Money;
import domain.wallet.Wallet;
import domain.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wallet.usecase.WalletChargeUseCase;
import wallet.usecase.WalletCreateUseCase;
import wallet.usecase.WalletQueryUseCase;

@Service
@RequiredArgsConstructor
public class WalletService implements WalletCreateUseCase, WalletQueryUseCase, WalletChargeUseCase {

    private final WalletRepository walletRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public Wallet createWallet(Long memberId) {
        Wallet wallet = Wallet.create(memberId, Money.zero());

        return walletRepository.save(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public Wallet getWallet(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 지갑입니다."));
    }

    @Override
    @Transactional(readOnly = true)
    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByMemberId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않거나 사용자의 지갑이 존재하지 않습니다."));
    }

    @Override
    @Transactional
    public void charge(
            Long memberId,
            Money amount,
            String transactionType,
            String referenceType,
            Long referenceId
    ) {
        Wallet wallet = getWalletByUserId(memberId);
        wallet.charge(amount);

        walletRepository.save(wallet);

        eventPublisher.publish(
                new WalletChargeCompletedEvent(
                        wallet.getId(),
                        transactionType,
                        amount,
                        wallet.getBalance(),
                        referenceType,
                        referenceId
                )
        );
    }

}
