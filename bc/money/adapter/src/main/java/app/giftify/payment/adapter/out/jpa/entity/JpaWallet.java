package app.giftify.payment.adapter.out.jpa.entity;

import app.giftify.shared.domain.vo.Money;
import app.giftify.support.jpa.BaseJpaEntity;
import domain.wallet.WalletSnapshot;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Entity
@EntityListeners(value = AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class JpaWallet extends BaseJpaEntity {
    @Column(unique = true)
    private Long memberId;

    @Column
    private BigDecimal balance;

    private JpaWallet(Long memberId, Money balance) {
        this.memberId = memberId;
        this.balance = balance.amount();
    }

    public static JpaWallet from(WalletSnapshot wallet) {
        return new JpaWallet(
                wallet.memberId(),
                wallet.balance()
        );
    }

    public WalletSnapshot toSnapshot() {
        return new WalletSnapshot(
                super.getId(),
                memberId,
                Money.of(balance),
                super.getCreatedAt(),
                super.getUpdatedAt()
        );
    }

    public void updateFrom(WalletSnapshot wallet) {
        this.balance = wallet.balance().amount();
    }
}
