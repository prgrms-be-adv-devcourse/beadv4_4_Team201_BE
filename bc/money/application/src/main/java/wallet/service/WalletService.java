package wallet.service;

import app.giftify.shared.domain.vo.Money;
import domain.exception.DuplicateTransactionException;
import domain.exception.WalletNotFoundException;
import domain.wallet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import walletHistory.port.WalletHistoryRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService implements WalletCreateUseCase, WalletQueryUseCase, WalletChargeUseCase, WalletWithdrawUseCase {

    private final WalletRepository walletRepository;
    private final WalletHistoryRepository walletHistoryRepository;

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
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }

    @Override
    @Transactional(readOnly = true)
    public Wallet getWalletByMemberId(Long memberId) {
        return walletRepository.findByMemberId(memberId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found. memberId=" + memberId));
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
        checkAlreadyProcessed(referenceType, referenceId);

        Wallet wallet = getWalletByMemberId(memberId);
        wallet.charge(amount);

        walletRepository.save(wallet);
        walletHistoryRepository.record(
                wallet.getId(),
                transactionType,
                amount,
                wallet.getBalance(),
                referenceType,
                referenceId
        );
    }

    @Override
    @Transactional
    public void withdraw(
            Long memberId,
            Money amount,
            String transactionType,
            String referenceType,
            Long referenceId
    ) {
        checkAlreadyProcessed(referenceType, referenceId);

        Wallet wallet = getWalletByMemberId(memberId);
        wallet.withdraw(amount);

        walletRepository.save(wallet);
        walletHistoryRepository.record(
                wallet.getId(),
                transactionType,
                amount,
                wallet.getBalance(),
                referenceType,
                referenceId
        );
    }

    private void checkAlreadyProcessed(String referenceType, Long referenceId) {
        if (walletHistoryRepository.existsByReferenceIdAndReferenceType(referenceId, referenceType)) {
            throw new DuplicateTransactionException(referenceType, referenceId);
        }
    }
}
