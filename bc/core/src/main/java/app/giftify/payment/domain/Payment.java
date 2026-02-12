package app.giftify.payment.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import app.giftify.payment.domain.event.PaymentCancelFailedEvent;
import app.giftify.payment.domain.event.PaymentCanceledEvent;
import app.giftify.payment.domain.event.PaymentConfirmedEvent;
import app.giftify.payment.domain.event.PaymentFailedEvent;
import app.giftify.payment.domain.event.PaymentReceivedEvent;
import app.giftify.payment.domain.event.PaymentRefundedEvent;
import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

public class Payment extends BaseDomainModel {
	private final PaymentType type;
	private final PaymentMethod method;
	private final String orderId; // uuid 를 생성할 때 앞쪽에 타임스탬프 포함하면 됨. 혹은 UUID7 스펙으로 생성하거나
	private final Long memberId;
	private final Money originAmount;
	private final Money paidAmount;
	private Money refundedAmount;
	private final List<OrderItemSnapshot> orderItems;

	private PaymentStatus status;
	private String paymentKey;
	private String lastTransactionKey; // NOTE :: 무조건 주는 값이니까 받아서 저장하도록 수정, 리스트가 되어야 하나?
	private String approveCode;
	private LocalDateTime paidAt;// NOTE :: lastModifiedAt 으로 통일, 외부로 나갈때 맥락에 따라 다르게 사용하도록 가이드
	private final LocalDateTime createdAt; // NOTE :: lastModifiedAt 으로 통일, 외부로 나갈때 맥락에 따라 다르게 사용하도록 가이드

	private Payment(Long id, PaymentType type, PaymentMethod method,
		String orderId, Long memberId,
		Money originAmount, Money paidAmount, Money refundedAmount, List<OrderItemSnapshot> orderItems,
		PaymentStatus status, String paymentKey, String lastTransactionKey, String approveCode,
		LocalDateTime paidAt, LocalDateTime createdAt
	) {
		super(id);
		this.orderId = orderId;
		this.memberId = memberId;
		this.type = type;
		this.method = method;
		this.originAmount = originAmount;
		this.paidAmount = paidAmount;
		this.refundedAmount = refundedAmount != null ? refundedAmount : Money.zero();
		this.orderItems = List.copyOf(orderItems);
		this.status = status;
		this.paymentKey = paymentKey;
		this.lastTransactionKey = lastTransactionKey;
		this.approveCode = approveCode;
		this.paidAt = paidAt;
		this.createdAt = createdAt;
	}

	// ========== 정적 팩토리 메서드 ========== //

	public static Builder builder() {
		return new Builder();
	}

	// ========== 상태 변경 메서드 ========== //

	public void markAsPaid(String paymentKey, String approveCode, String lastTransactionKey, LocalDateTime paidAt) {
		if (!PaymentEventType.PAID.canApply(this.status)) {
			throw new PaymentException(PaymentErrorCode.NOT_PAYABLE,
				"[Payment] 결제 완료 불가능한 상태입니다: " + this.status);
		}
		this.status = PaymentEventType.PAID.getResultStatus();
		this.paymentKey = paymentKey;
		this.approveCode = approveCode;
		this.lastTransactionKey = lastTransactionKey;
		this.paidAt = paidAt;

		registerEvent(new PaymentConfirmedEvent(
			getId(), getMemberId(), getOrderId(), getType(), getPaidAmount(), paidAt
		));
	}

	public void markAsRefunded(Money refundAmount, LocalDateTime occurredAt, String reason) {
		if (!PaymentEventType.REFUNDED.canApply(this.status)) {
			throw new PaymentException(PaymentErrorCode.NOT_REFUNDABLE,
				"[Payment] 환불 불가능한 상태입니다: " + this.status);
		}

		Money remainingRefundable = this.paidAmount.minus(this.refundedAmount);
		if (refundAmount.isGreaterThan(remainingRefundable)) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[Payment] 환불 금액이 남은 환불 가능 금액을 초과합니다. 요청: " + refundAmount + ", 가능: " + remainingRefundable);
		}

		this.refundedAmount = this.refundedAmount.plus(refundAmount);

		if (this.refundedAmount.equals(this.paidAmount)) {
			this.status = PaymentEventType.REFUNDED.getResultStatus();
		}

		registerEvent(new PaymentRefundedEvent(
			getId(), getMemberId(), getOrderId(), getType(), refundAmount, reason, occurredAt
		));
	}

	public void markAsCanceled(LocalDateTime occurredAt, String reason) {
		if (!PaymentEventType.CANCELED.canApply(this.status)) {
			throw new PaymentException(PaymentErrorCode.NOT_CANCELABLE,
				"[Payment] 취소 불가능한 상태입니다: " + this.status);
		}
		this.status = PaymentEventType.CANCELED.getResultStatus();

		registerEvent(new PaymentCanceledEvent(
			getId(), getMemberId(), getOrderId(), getType(), getPaidAmount(), reason, occurredAt
		));
	}

	public void markAsFailed(LocalDateTime occurredAt) {
		if (!PaymentEventType.FAILED.canApply(this.status)) {
			throw new PaymentException(PaymentErrorCode.NOT_FAILABLE,
				"[Payment] 대기 중인 결제만 실패 처리할 수 있습니다. 현재 상태: " + this.status);
		}
		this.status = PaymentEventType.FAILED.getResultStatus();

		registerEvent(new PaymentFailedEvent(getId(), getOrderId(), occurredAt));
	}

	public void markAsReceived(LocalDateTime occurredAt) {
		if (!PaymentEventType.RECEIVED.canApply(this.status)) {
			throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS,
				"[Payment] 수령 확정 불가능한 상태입니다: " + this.status);
		}
		this.status = PaymentEventType.RECEIVED.getResultStatus();

		registerEvent(new PaymentReceivedEvent(
			getId(), getMemberId(), getOrderId(), occurredAt
		));
	}

	public void recordCancelFailed(String errorMetadata, LocalDateTime occurredAt) {
		if (!PaymentEventType.CANCEL_FAILED.canApply(this.status)) {
			throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS,
				"[Payment] 취소 실패 기록은 PAID 상태에서만 가능합니다. 현재 상태: " + this.status);
		}

		registerEvent(new PaymentCancelFailedEvent(getId(), getOrderId(), errorMetadata, occurredAt));
	}

	// ========== 상태 조회 메서드 ========== //

	public boolean isRefundable() {
		return PaymentEventType.REFUNDED.canApply(this.status) && this.refundedAmount.isLessThan(this.paidAmount);
	}

	public boolean isCancelable() {
		return PaymentEventType.CANCELED.canApply(this.status);
	}

	public boolean isReceivable() {
		return PaymentEventType.RECEIVED.canApply(this.status);
	}

	/**
	 * 해당 회원이 이 결제의 소유자인지 확인합니다.
	 *
	 * @param requesterId 요청자 회원 ID
	 * @return 소유자이면 true
	 */
	public boolean isOwnedBy(Long requesterId) {
		return this.memberId != null && this.memberId.equals(requesterId);
	}

	// ========== Getter ========== //

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

	public Money getRefundedAmount() {
		return refundedAmount;
	}

	public List<OrderItemSnapshot> getOrderItems() {
		return orderItems;
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

	public String getLastTransactionKey() {
		return lastTransactionKey;
	}

	public String getApproveCode() {
		return approveCode;
	}

	public LocalDateTime getPaidAt() {
		return paidAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	// ========== equals / hashCode ========== //

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Payment payment))
			return false;
		if (getId() != null && payment.getId() != null) {
			return Objects.equals(getId(), payment.getId());
		}
		return Objects.equals(orderId, payment.orderId);
	}

	@Override
	public int hashCode() {
		if (getId() != null) {
			return Objects.hash(getId());
		}
		return Objects.hash(orderId);
	}

	// ========== Builder ========== //

	public static class Builder {
		private Long id;
		private String orderId;
		private Long memberId;
		private Money originAmount;
		private Money paidAmount;
		private Money refundedAmount;
		private List<OrderItemSnapshot> orderItems;
		private PaymentStatus status;
		private PaymentType type;
		private PaymentMethod method;
		private String paymentKey;
		private String lastTransactionKey;
		private String approveCode;
		private LocalDateTime paidAt;
		private LocalDateTime createdAt;

		public Builder id(Long id) {
			this.id = id;
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

		public Builder refundedAmount(Money refundedAmount) {
			this.refundedAmount = refundedAmount;
			return this;
		}

		public Builder orderItems(List<OrderItemSnapshot> orderItems) {
			this.orderItems = orderItems;
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

		public Builder lastTransactionKey(String lastTransactionKey) {
			this.lastTransactionKey = lastTransactionKey;
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

		public Builder createdAt(LocalDateTime createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Payment build() {
			validate();
			return new Payment(
				id,
				type, method,
				orderId, memberId,
				originAmount, paidAmount, refundedAmount, orderItems,
				status, paymentKey, lastTransactionKey, approveCode,
				paidAt, createdAt
			);
		}

		private void validate() {
			validateRequiredFields();
			validateAmountInvariant();
			validateOrderItemsIfRequired();
		}

		private void validateRequiredFields() {
			requireNonBlank(orderId, "orderId");
			requireNonNull(memberId, "memberId");
			requireNonNull(type, "type");
			requireNonNull(method, "method");
			requireNonNull(originAmount, "originAmount");
			requireNonNull(paidAmount, "paidAmount");
			requireNonNull(status, "status");
		}

		private void requireNonBlank(String value, String fieldName) {
			if (value == null || value.isBlank()) {
				throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
					"[Payment] " + fieldName + "는 필수입니다.");
			}
		}

		private void requireNonNull(Object value, String fieldName) {
			if (value == null) {
				throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
					"[Payment] " + fieldName + "는 필수입니다.");
			}
		}

		private void validateAmountInvariant() {
			if (paidAmount.isGreaterThan(originAmount)) {
				throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
					"[Payment] paidAmount는 originAmount를 초과할 수 없습니다.");
			}
		}

		private void validateOrderItemsIfRequired() {
			if (type == PaymentType.DEPOSIT_CHARGE) {
				return;
			}

			if (orderItems == null || orderItems.isEmpty()) {
				throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
					"[Payment] orderItems는 필수입니다.");
			}

			Money itemsTotal = orderItems.stream()
				.map(OrderItemSnapshot::amount)
				.reduce(Money.zero(), Money::plus);

			if (!itemsTotal.equals(originAmount)) {
				throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
					"[Payment] orderItems 합계와 originAmount가 일치하지 않습니다.");
			}
		}
	}

	// ========== 정적 팩토리 메서드  ========== //

	public static Payment create(
		PaymentCreateContext context,
		Money originAmount,
		Money paidAmount,
		List<OrderItemSnapshot> orderItems
	) {
		return builder()
			.orderId(context.orderId())
			.memberId(context.memberId())
			.type(context.type())
			.method(context.method())
			.originAmount(originAmount)
			.paidAmount(paidAmount)
			.orderItems(orderItems)
			.status(PaymentStatus.PENDING)
			.build();
	}
}
