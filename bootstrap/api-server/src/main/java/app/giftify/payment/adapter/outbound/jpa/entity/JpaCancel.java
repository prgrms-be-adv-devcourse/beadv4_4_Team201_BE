package app.giftify.payment.adapter.outbound.jpa.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import app.giftify.payment.domain.Cancel;
import app.giftify.support.common.money.Money;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_cancels")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaCancel extends BaseJpaEntity {

	@Column(name = "payment_id", nullable = false)
	private Long paymentId;

	@Column(name = "transaction_key", unique = true, nullable = false, length = 255)
	private String transactionKey;

	@Column(name = "cancel_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal cancelAmount;

	@Column(name = "cancel_reason", nullable = false, length = 500)
	private String cancelReason;

	@Column(name = "canceled_at", nullable = false)
	private LocalDateTime canceledAt;

	private JpaCancel(
		Long paymentId,
		String transactionKey,
		BigDecimal cancelAmount,
		String cancelReason,
		LocalDateTime canceledAt
	) {
		this.paymentId = paymentId;
		this.transactionKey = transactionKey;
		this.cancelAmount = cancelAmount;
		this.cancelReason = cancelReason;
		this.canceledAt = canceledAt;
	}

	public static JpaCancel from(Cancel cancel) {
		return new JpaCancel(
			cancel.getPaymentId(),
			cancel.getTransactionKey(),
			cancel.getCancelAmount().amount(),
			cancel.getCancelReason(),
			cancel.getCanceledAt()
		);
	}

	public Cancel toDomain() {
		return Cancel.restore(
			super.getId(),
			paymentId,
			transactionKey,
			Money.of(cancelAmount),
			cancelReason,
			canceledAt
		);
	}
}
