package app.giftify.payment.adapter.out.jpa.entity;

import app.giftify.shared.domain.vo.Money;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

// 이력 엔티티는 수정되어선 안됨
// BaseJpaEntity 상속 X
@Entity
@Getter
@Immutable
@NoArgsConstructor
public class JpaWalletHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(updatable = false)
    private LocalDateTime occurredAt;

    public JpaWalletHistory(Long walletId, String transactionType, Money amount, Money balanceAfter, String referenceType, Long referenceId, LocalDateTime occurredAt) {
        this.walletId = walletId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.occurredAt = occurredAt;
    }
}
