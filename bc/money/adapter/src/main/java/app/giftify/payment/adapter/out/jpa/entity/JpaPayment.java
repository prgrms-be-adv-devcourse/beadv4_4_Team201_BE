package app.giftify.payment.adapter.out.jpa.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import app.giftify.shared.domain.payment.PaymentType;
import app.giftify.support.jpa.BaseJpaEntity;
import domain.payment.PaymentMethod;
import domain.payment.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PAYMENT_PAYMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaPayment extends BaseJpaEntity {

	@Column(nullable = false)
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@Column(nullable = false)
	private BigDecimal amount;

	private Long fundingId;

	private String pgTransactionId;

	@Enumerated(EnumType.STRING)
	private PaymentMethod method;

	private LocalDateTime paidAt;
	private LocalDateTime refundedAt;
	private LocalDateTime settledAt;

	private JpaPayment(Long userId, PaymentType type, PaymentStatus status, BigDecimal amount, Long fundingId,
					   String pgTransactionId, PaymentMethod method,
					   LocalDateTime paidAt, LocalDateTime refundedAt, LocalDateTime settledAt) {
		this.userId = userId;
		this.type = type;
		this.status = status;
		this.amount = amount;
		this.fundingId = fundingId;
		this.pgTransactionId = pgTransactionId;
		this.method = method;
		this.paidAt = paidAt;
		this.refundedAt = refundedAt;
		this.settledAt = settledAt;
	}

	public void update(domain.payment.Payment domain) {
		this.userId = domain.getUserId();
		this.type = domain.getType();
		this.status = domain.getStatus();
		this.amount = domain.getAmount().amount();
		this.fundingId = domain.getFundingId();
		this.pgTransactionId = domain.getPgTransactionId();
		this.method = domain.getMethod();
		this.paidAt = domain.getPaidAt();
		this.refundedAt = domain.getRefundedAt();
		this.settledAt = domain.getSettledAt();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Long userId;
		private PaymentType type;
		private PaymentStatus status;
		private BigDecimal amount;
		private Long fundingId;
		private String pgTransactionId;
		private PaymentMethod method;
		private LocalDateTime paidAt;
		private LocalDateTime refundedAt;
		private LocalDateTime settledAt;

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

		public JpaPayment.Builder fundingId(Long fundingId) {
			this.fundingId = fundingId;
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

		public JpaPayment.Builder paidAt(LocalDateTime paidAt) {
			this.paidAt = paidAt;
			return this;
		}

		public JpaPayment.Builder refundedAt(LocalDateTime refundedAt) {
			this.refundedAt = refundedAt;
			return this;
		}

		public JpaPayment.Builder settledAt(LocalDateTime settledAt) {
			this.settledAt = settledAt;
			return this;
		}

		public JpaPayment build() {
			return new JpaPayment(
				userId,
				type,
				status,
				amount,
				fundingId,
				pgTransactionId,
				method,
				paidAt,
				refundedAt,
				settledAt
			);
		}
	}
}
