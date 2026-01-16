package app.giftify.shared.domain.event.wallet;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

// todo: 하나만 WalletEvent에 EventType을 통해 이벤트를 구분? 아님 각 개별 이벤트 생성?
public class WalletWithdrawnEvent extends BaseDomainEvent {
    private final Long walletId;
    private final String transactionType;
    private final Money amount;
    private final Money balanceAfter;
    private final String referenceType;
    private final Long referenceId;

    public WalletWithdrawnEvent(Long walletId, String transactionType, Money amount, Money balanceAfter, String referenceType, Long referenceId) {
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
