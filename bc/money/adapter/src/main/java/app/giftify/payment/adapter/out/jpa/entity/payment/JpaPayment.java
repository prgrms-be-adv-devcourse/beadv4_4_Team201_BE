package app.giftify.payment.adapter.out.jpa.entity.payment;

import java.math.BigDecimal;

import app.giftify.shared.domain.event.payment.PaymentType;
import app.giftify.support.jpa.BaseJpaEntity;
import domain.payment.PaymentMethod;
import domain.payment.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "PAYMENT_PAYMENT")
public class JpaPayment extends BaseJpaEntity {

	@Column(nullable = false)
	private Long userId;

	private String pgTransactionId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@Column(nullable = false)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	private PaymentMethod method;

	public static Builder builder() {
		return new Builder();
	}

	protected JpaPayment() {
	}

	private JpaPayment(Long userId, String pgTransactionId,
		PaymentType type, PaymentStatus status,
		BigDecimal amount,
		PaymentMethod method
	) {
		this.userId = userId;
		this.type = type;
		this.status = status;
		this.amount = amount;
		this.pgTransactionId = pgTransactionId;
		this.method = method;
	}

	public void update(domain.payment.Payment domain) {
		this.userId = domain.getUserId();
		this.type = domain.getType();
		this.status = domain.getStatus();
		this.amount = domain.getAmount().amount();
		this.pgTransactionId = domain.getPgTransactionId();
		this.method = domain.getMethod();
	}

	public Long getUserId() {
		return userId;
	}

	public String getPgTransactionId() {
		return pgTransactionId;
	}

	public PaymentType getType() {
		return type;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public PaymentMethod getMethod() {
		return method;
	}

	public static class Builder {
		private Long userId;
		private String pgTransactionId;
		private PaymentType type;
		private PaymentStatus status;
		private BigDecimal amount;
		private PaymentMethod method;

		public JpaPayment.Builder userId(Long userId) {
			this.userId = userId;
			return this;
		}

		public JpaPayment.Builder type(PaymentType type) {
			this.type = type;
			return this;
		}

		public JpaPayment.Builder status(PaymentStatus status) {
			this.status = status;
			return this;
		}

		public JpaPayment.Builder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public JpaPayment.Builder pgTransactionId(String pgTransactionId) {
			this.pgTransactionId = pgTransactionId;
			return this;
		}

		public JpaPayment.Builder method(PaymentMethod method) {
			this.method = method;
			return this;
		}

		public JpaPayment build() {
			return new JpaPayment(
				userId,
				pgTransactionId,
				type,
				status,
				amount,
				method
			);
		}
	}
}
