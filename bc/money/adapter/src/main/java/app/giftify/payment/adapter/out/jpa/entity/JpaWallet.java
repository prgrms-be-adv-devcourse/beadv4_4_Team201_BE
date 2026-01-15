package app.giftify.payment.adapter.out.jpa.entity;

import domain.wallet.WalletSnapshot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import app.giftify.shared.domain.vo.Money;

import java.time.LocalDateTime;

@Entity
@EntityListeners(value = AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class JpaWallet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long memberId;

    @Embedded
    private Money balance;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;

    private JpaWallet(Long id, Long memberId, Money balance, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.memberId = memberId;
        this.balance = balance;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public static JpaWallet from(WalletSnapshot wallet) {
        return new JpaWallet(
                wallet.id(),
                wallet.memberId(),
                wallet.balance(),
                wallet.createdAt(),
                wallet.modifiedAt()
        );
    }

    public WalletSnapshot toSnapshot() {
        return new WalletSnapshot(
                id,
                memberId,
                balance,
                createdAt,
                modifiedAt
        );
    }

    public void updateFrom(WalletSnapshot wallet) {
        this.balance = wallet.balance();
    }
}
