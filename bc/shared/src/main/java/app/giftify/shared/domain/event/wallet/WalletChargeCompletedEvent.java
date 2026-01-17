package app.giftify.shared.domain.event.wallet;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

public class WalletChargeCompletedEvent extends BaseDomainEvent {
    private final Long walletId;
    private final String transactionType;
    private final Money amount;
    private final Money balanceAfter;
    private final String referenceType;
    private final Long referenceId;

    public WalletChargeCompletedEvent(Long walletId, String transactionType, Money amount, Money balanceAfter, String referenceType, Long referenceId) {
        this.walletId = walletId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }

    public Long getWalletId() {
        return walletId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public Money getAmount() {
        return amount;
    }

    public Money getBalanceAfter() {
        return balanceAfter;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }
}
