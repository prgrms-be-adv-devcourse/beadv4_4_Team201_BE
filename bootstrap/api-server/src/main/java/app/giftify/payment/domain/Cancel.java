package app.giftify.payment.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import app.giftify.support.common.base.BaseDomainModel;
import app.giftify.support.common.money.Money;

public class Cancel extends BaseDomainModel {

	private final Long paymentId;
	private final String transactionKey;
	private final Money cancelAmount;
	private final String cancelReason;
	private final LocalDateTime canceledAt;

	private Cancel(
		Long id,
		Long paymentId,
		String transactionKey,
		Money cancelAmount,
		String cancelReason,
		LocalDateTime canceledAt
	) {
		super(id);
		this.paymentId = paymentId;
		this.transactionKey = transactionKey;
		this.cancelAmount = cancelAmount;
		this.cancelReason = cancelReason;
		this.canceledAt = canceledAt;
	}

	// ========== 정적 팩토리 메서드 ========== //

	public static Cancel create(
		Long paymentId,
		String transactionKey,
		Money cancelAmount,
		String cancelReason,
		LocalDateTime canceledAt
	) {
		return new Cancel(null, paymentId, transactionKey, cancelAmount, cancelReason, canceledAt);
	}

	public static Cancel restore(
		Long id,
		Long paymentId,
		String transactionKey,
		Money cancelAmount,
		String cancelReason,
		LocalDateTime canceledAt
	) {
		return new Cancel(id, paymentId, transactionKey, cancelAmount, cancelReason, canceledAt);
	}

	// ========== Getter ========== //

	public Long getPaymentId() {
		return paymentId;
	}

	public String getTransactionKey() {
		return transactionKey;
	}

	public Money getCancelAmount() {
		return cancelAmount;
	}

	public String getCancelReason() {
		return cancelReason;
	}

	public LocalDateTime getCanceledAt() {
		return canceledAt;
	}

	// ========== equals / hashCode ========== //

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Cancel that))
			return false;
		if (getId() != null && that.getId() != null) {
			return Objects.equals(getId(), that.getId());
		}
		return Objects.equals(transactionKey, that.transactionKey)
			&& Objects.equals(canceledAt, that.canceledAt);
	}

	@Override
	public int hashCode() {
		if (getId() != null) {
			return Objects.hash(getId());
		}
		return Objects.hash(transactionKey, canceledAt);
	}
}
