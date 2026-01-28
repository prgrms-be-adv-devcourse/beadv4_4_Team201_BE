package app.giftify.wallet.adapter.outbound.jpa.entity;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import app.giftify.wallet.domain.WalletSnapshot;
import app.giftify.shared.domain.vo.Money;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wallet", schema = "payment")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaWallet extends BaseJpaEntity {

	@Column(unique = true, nullable = false)
	private Long memberId;

	@Column(nullable = false)
	private BigDecimal balance;

	@Version
	private Long version;

	private JpaWallet(Long memberId, BigDecimal balance) {
		this.memberId = memberId;
		this.balance = balance;
	}

	public static JpaWallet from(WalletSnapshot snapshot) {
		return new JpaWallet(
			snapshot.memberId(),
			snapshot.balance().amount()
		);
	}

	public WalletSnapshot toSnapshot() {
		return new WalletSnapshot(
			super.getId(),
			memberId,
			Money.of(balance)
		);
	}

	public void updateFrom(WalletSnapshot snapshot) {
		this.balance = snapshot.balance().amount();
	}
}
