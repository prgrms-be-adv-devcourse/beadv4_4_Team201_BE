package app.giftify.payment.adapter.wallet.adapter.outbound.jpa.entity;

import app.giftify.shared.domain.vo.Money;
import app.giftify.support.jpa.BaseJpaHistoryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
public class JpaWalletHistory extends BaseJpaHistoryEntity {

    @Column(updatable = false)
    private Long walletId;

    @Column(updatable = false)
    private String transactionType;

    @Column
    private BigDecimal amount;

    @Column
    private BigDecimal balanceAfter;

    @Column(updatable = false)
    private String referenceType;

    @Column(updatable = false)
    private Long referenceId;

    public JpaWalletHistory(
            Long walletId,
            String transactionType,
            Money amount,
            Money balanceAfter,
            String referenceType,
            Long referenceId
    ) {
        this.walletId = walletId;
        this.transactionType = transactionType;
        this.amount = amount.amount();
        this.balanceAfter = balanceAfter.amount();
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }
}
