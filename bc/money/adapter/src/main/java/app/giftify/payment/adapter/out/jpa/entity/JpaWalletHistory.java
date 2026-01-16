package app.giftify.payment.adapter.out.jpa.entity;

import app.giftify.shared.domain.vo.Money;
import app.giftify.support.jpa.BaseJpaHistoryEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class JpaWalletHistory extends BaseJpaHistoryEntity {

    @Column(updatable = false)
    private Long walletId;

    @Column(updatable = false)
    private String transactionType;

    @Embedded
    private Money amount;

    @Embedded
    private Money balanceAfter;

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
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }
}
