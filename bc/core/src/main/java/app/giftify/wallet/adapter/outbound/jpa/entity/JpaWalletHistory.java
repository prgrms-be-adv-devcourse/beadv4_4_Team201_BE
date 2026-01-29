package app.giftify.wallet.adapter.outbound.jpa.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import app.giftify.wallet.domain.WalletHistory;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wallet_history")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaWalletHistory extends BaseJpaEntity {

	@Column(nullable = false)
	private Long walletId;

	@Column(nullable = false)
	private String transactionType;

	@Column(nullable = false)
	private BigDecimal amount;

	@Column(nullable = false)
	private BigDecimal balanceAfter;

	@Column(nullable = false)
	private String referenceType;

	@Column(nullable = false)
	private String referenceId;

	@Column(nullable = false)
	private LocalDateTime occurredAt;

	private JpaWalletHistory(
		Long walletId,
		String transactionType,
		BigDecimal amount,
		BigDecimal balanceAfter,
		String referenceType,
		String referenceId,
		LocalDateTime occurredAt
	) {
		this.walletId = walletId;
		this.transactionType = transactionType;
		this.amount = amount;
		this.balanceAfter = balanceAfter;
		this.referenceType = referenceType;
		this.referenceId = referenceId;
		this.occurredAt = occurredAt;
	}

	public static JpaWalletHistory from(WalletHistory history) {
		return new JpaWalletHistory(
			history.getWalletId(),
			history.getTransactionType().name(),
			history.getAmount().amount(),
			history.getBalanceAfter().amount(),
			history.getReferenceType().name(),
			history.getReferenceId(),
			history.getOccurredAt()
		);
	}
}
