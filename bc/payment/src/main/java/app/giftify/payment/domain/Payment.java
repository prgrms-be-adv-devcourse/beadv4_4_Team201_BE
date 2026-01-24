package app.giftify.payment.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

public class Payment extends BaseDomainModel {
	private final String idempotencyKey;
	private final String orderId;
	private final Long memberId;
	private final PaymentType type;
	private final PaymentMethod method;
	private final Money originAmount;
	private final Money paidAmount;

	private PaymentStatus status;
	private String paymentKey;
	private String approveCode;
	private LocalDateTime paidAt;

	private Payment(Long id, String idempotencyKey, String orderId, Long memberId,
		PaymentType type, PaymentMethod method,
		Money originAmount, Money paidAmount,
		PaymentStatus status, String paymentKey, String approveCode,
		LocalDateTime paidAt
	) {
		super(id);
		this.idempotencyKey = idempotencyKey;
		this.orderId = orderId;
		this.memberId = memberId;
		this.type = type;
		this.method = method;
		this.originAmount = originAmount;
		this.paidAmount = paidAmount;
		this.status = status;
		this.paymentKey = paymentKey;
		this.approveCode = approveCode;
		this.paidAt = paidAt;
	}

	// ========== 정적 팩토리 메서드 ========== //

	public static Builder builder() {
		return new Builder();
	}

	// ========== 상태 변경 메서드 ========== //

	/**
	 * 결제를 완료 처리합니다.
	 * PENDING 상태에서만 호출 가능합니다.
	 *
	 * @param paymentKey  PG사 결제 키
	 * @param approveCode PG사 승인 코드
	 * @throws IllegalStateException 상태가 PENDING이 아닌 경우
	 */
	public void markAsPaid(String paymentKey, String approveCode) {
		if (!this.status.equals(PaymentStatus.PENDING)) {
			throw new PaymentException(PaymentErrorCode.NOT_PAYABLE, "[Payment] 결제 완료 불가능한 상태입니다: " + this.status);
		}
		this.status = PaymentStatus.PAID;
		this.paymentKey = paymentKey;
		this.approveCode = approveCode;
		this.paidAt = LocalDateTime.now();
	}

	/**
	 * 결제를 환불 처리합니다.
	 * PAID 상태에서만 호출 가능합니다.
	 *
	 * @throws IllegalStateException 환불 불가능한 상태인 경우
	 */
	public void refund() {
		if (!this.isRefundable()) {
			throw new PaymentException(PaymentErrorCode.NOT_REFUNDABLE,
				"[Payment] 환불 불가능한 상태입니다: " + this.status);
		}
		this.status = PaymentStatus.REFUNDED;
	}

	/**
	 * 결제를 취소 처리합니다.
	 * PENDING 상태에서만 호출 가능합니다.
	 *
	 * @throws IllegalStateException 취소 불가능한 상태인 경우
	 */
	public void cancel() {
		if (!this.isCancelable()) {
			throw new PaymentException(PaymentErrorCode.NOT_CANCELABLE, "[Payment] 취소 불가능한 상태입니다: " + this.status);
		}
		this.status = PaymentStatus.CANCELED;
	}

	/**
	 * 결제를 실패 처리합니다.
	 * PENDING 상태에서만 호출 가능합니다.
	 *
	 * @throws IllegalStateException 상태가 PENDING이 아닌 경우
	 */
	public void markAsFailed() {
		if (this.status != PaymentStatus.PENDING) {
			throw new PaymentException(PaymentErrorCode.NOT_FAILABLE,
				"[Payment] 대기 중인 결제만 실패 처리할 수 있습니다. 현재 상태: " + this.status);
		}
		this.status = PaymentStatus.FAILED;
	}

	// ========== 상태 조회 메서드 ========== //

	public boolean isRefundable() {
		return this.status == PaymentStatus.PAID;
	}

	public boolean isCancelable() {
		return this.status == PaymentStatus.PENDING;
	}

	// ========== Getter ========== //

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public String getOrderId() {
		return orderId;
	}

	public Long getMemberId() {
		return memberId;
	}

	public Money getOriginAmount() {
		return originAmount;
	}

	public Money getPaidAmount() {
		return paidAmount;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public PaymentType getType() {
		return type;
	}

	public PaymentMethod getMethod() {
		return method;
	}

	public String getPaymentKey() {
		return paymentKey;
	}

	public String getApproveCode() {
		return approveCode;
	}

	public LocalDateTime getPaidAt() {
		return paidAt;
	}

	// ========== equals / hashCode ========== //

	/**
	 * Entity 동일성은 id로 판단합니다.
	 * id가 null인 경우(비영속 상태)에는 idempotencyKey를 비교합니다.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Payment payment))
			return false;

		// id가 있으면 id로 비교
		if (getId() != null && payment.getId() != null) {
			return Objects.equals(getId(), payment.getId());
		}

		// 비영속 상태에서는 비즈니스 키(idempotencyKey)로 비교
		return Objects.equals(idempotencyKey, payment.idempotencyKey);
	}

	@Override
	public int hashCode() {
		// id가 있으면 id 기반, 없으면 idempotencyKey 기반
		if (getId() != null) {
			return Objects.hash(getId());
		}
		return Objects.hash(idempotencyKey);
	}

	// ========== Builder ========== //

	public static class Builder {
		private Long id;
		private String idempotencyKey;
		private String orderId;
		private Long memberId;
		private Money originAmount;
		private Money paidAmount;
		private PaymentStatus status;
		private PaymentType type;
		private PaymentMethod method;
		private String paymentKey;
		private String approveCode;
		private LocalDateTime paidAt;

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder idempotencyKey(String idempotencyKey) {
			this.idempotencyKey = idempotencyKey;
			return this;
		}

		public Builder orderId(String orderId) {
			this.orderId = orderId;
			return this;
		}

		public Builder memberId(Long memberId) {
			this.memberId = memberId;
			return this;
		}

		public Builder originAmount(Money originAmount) {
			this.originAmount = originAmount;
			return this;
		}

		public Builder paidAmount(Money paidAmount) {
			this.paidAmount = paidAmount;
			return this;
		}

		public Builder status(PaymentStatus status) {
			this.status = status;
			return this;
		}

		public Builder type(PaymentType type) {
			this.type = type;
			return this;
		}

		public Builder method(PaymentMethod method) {
			this.method = method;
			return this;
		}

		public Builder paymentKey(String paymentKey) {
			this.paymentKey = paymentKey;
			return this;
		}

		public Builder approveCode(String approveCode) {
			this.approveCode = approveCode;
			return this;
		}

		public Builder paidAt(LocalDateTime paidAt) {
			this.paidAt = paidAt;
			return this;
		}

		public Payment build() {
			validate();
			return new Payment(
				id,
				idempotencyKey, orderId, memberId,
				type, method,
				originAmount, paidAmount,
				status, paymentKey, approveCode,
				paidAt
			);
		}

		private void validate() {
			if (idempotencyKey == null || idempotencyKey.isBlank()) {
				throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
					"[Payment] idempotencyKey는 필수입니다.");
			}
			if (orderId == null || orderId.isBlank()) {
				throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
					"[Payment] orderId는 필수입니다.");
			}
			if (memberId == null) {
				throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
					"[Payment] memberId는 필수입니다.");
			}
			if (type == null) {
				throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
					"[Payment] type은 필수입니다.");
			}
			if (method == null) {
				throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
					"[Payment] method는 필수입니다.");
			}
			if (originAmount == null) {
				throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
					"[Payment] originAmount는 필수입니다.");
			}
			if (status == null) {
				throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
					"[Payment] status는 필수입니다.");
			}
		}
	}
}
