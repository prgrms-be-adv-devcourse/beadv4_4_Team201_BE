package domain.payment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.event.payment.PaymentType;
import app.giftify.shared.domain.vo.Money;

public class Payment extends BaseDomainModel {
	private final Long userId;
	private final PaymentType type;
	private PaymentStatus status;
	private final Money amount;
	private String pgTransactionId;
	private final PaymentMethod method;

	private final List<PaymentHistory> uncommittedHistory = new ArrayList<>();

	private Payment(
		Long id, Long userId, PaymentType type, PaymentStatus status,
		Money amount, String pgTransactionId, PaymentMethod method
	) {
		super(id);
		this.userId = userId;
		this.type = type;
		this.status = status;
		this.amount = amount;
		this.pgTransactionId = pgTransactionId;
		this.method = method;
	}

	public static Builder builder() {
		return new Builder();
	}

	// ========== 정적 팩토리 메서드 ========== //

	/**
	 * 결제를 생성합니다. 초기 상태는 항상 PENDING 입니다.
	 */
	public static Payment create(
		Long userId,
		PaymentType type,
		Money amount,
		PaymentMethod method
	) {
		LocalDateTime now = LocalDateTime.now();

		Payment payment = Payment.builder()
			.userId(userId)
			.type(type)
			.status(PaymentStatus.PENDING) // 항상 대기 상태로 시작
			.amount(amount)
			.method(method)
			.build();

		payment.uncommittedHistory.add(new PaymentHistory(
			null,           // paymentId - 저장 후 할당
			null,                   // idempotencyKey - CREATED는 null OK
			PaymentEventType.CREATED,
			now,
			null            // metadata
		));

		return payment;
	}

	// ========== 상태 변경 메서드 ========== //

	public PaymentHistory markAsPaid(String pgTransactionId) {
		if (this.status != PaymentStatus.PENDING) {
			throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS,
				"[Payment] 결제 대기(PENDING) 상태에서만 완료 처리할 수 있습니다. 현재 상태: " + this.status);
		}

		LocalDateTime now = LocalDateTime.now();
		this.status = PaymentStatus.PAID;
		this.pgTransactionId = pgTransactionId;

		PaymentHistory history = new PaymentHistory(
			getId(),
			pgTransactionId,
			PaymentEventType.PAID,
			now,
			"{\"pgTransactionId\":\"" + (pgTransactionId != null ? pgTransactionId : "") + "\"}"
		);

		this.uncommittedHistory.add(history);

		return history;
	}

	public PaymentHistory settle() {
		if (this.status != PaymentStatus.PAID) {
			throw new PaymentException(PaymentErrorCode.NOT_SETTLEABLE,
				"[Payment] 결제 완료(PAID) 상태에서만 확정할 수 있습니다. 현재 상태: " + this.status);
		}

		LocalDateTime now = LocalDateTime.now();
		this.status = PaymentStatus.SETTLED;

		PaymentHistory history = new PaymentHistory(
			getId(),
			null,
			PaymentEventType.SETTLED,
			now,
			null
		);

		this.uncommittedHistory.add(history);

		return history;
	}

	public PaymentHistory refund() {
		if (this.status == PaymentStatus.SETTLED) {
			throw new PaymentException(PaymentErrorCode.NOT_REFUNDABLE,
				"[Payment] 이미 정산(수령) 처리되어 환불할 수 없습니다.");
		}
		if (!this.status.canRefund()) {
			throw new PaymentException(PaymentErrorCode.NOT_REFUNDABLE,
				"[Payment] 환불 불가능한 상태입니다: " + this.status);
		}

		LocalDateTime now = LocalDateTime.now();
		this.status = PaymentStatus.REFUNDED;

		PaymentHistory history = new PaymentHistory(
			getId(),
			null,
			PaymentEventType.REFUNDED,
			now,
			null
		);

		this.uncommittedHistory.add(history);

		return history;
	}

	public PaymentHistory cancel(String metadata) {
		if (!this.status.canCancel()) {
			throw new PaymentException(PaymentErrorCode.NOT_CANCELABLE,
				"[Payment] 취소 불가능한 상태입니다: " + this.status);
		}

		LocalDateTime now = LocalDateTime.now();
		this.status = PaymentStatus.CANCELED;

		// PaymentHistory 생성 시 metadata 전달
		PaymentHistory history = new PaymentHistory(
			getId(),
			null, // pgTransactionId는 취소 시점엔 null (필요시 추가 확장 가능)
			PaymentEventType.CANCELED,
			now,
			metadata // <--- 이 부분에 저장
		);

		this.uncommittedHistory.add(history);
		return history;
	}

	/**
	 * @return 하위 호환성 제공용
	 */
	public PaymentHistory cancel() {
		return cancel(null);
	}

	public PaymentHistory markAsFailed() {
		if (this.status != PaymentStatus.PENDING) {
			throw new IllegalStateException("[Payment] 대기 중인 결제만 실패 처리할 수 있습니다. 현재 상태: " + this.status);
		}

		LocalDateTime now = LocalDateTime.now();
		this.status = PaymentStatus.FAILED;

		PaymentHistory history = new PaymentHistory(
			getId(),
			null,
			PaymentEventType.FAILED,
			now,
			null
		);

		this.uncommittedHistory.add(history);

		return history;
	}

	// ========== 이벤트 이력 관리 ========== //

	public List<PaymentHistory> getUncommittedHistory() {
		return Collections.unmodifiableList(uncommittedHistory);
	}

	public void clearUncommittedHistory() {
		uncommittedHistory.clear();
	}

	// ========== 상태 조회 메서드 ========== //

	public boolean isRefundable() {
		return this.status == PaymentStatus.PAID;
	}

	public boolean isCancelable() {
		return this.status == PaymentStatus.PENDING;
	}

	// ========== Getter ========== //

	public Long getPaymentId() {
		return getId();
	}

	public Long getUserId() {
		return userId;
	}

	public PaymentType getType() {
		return type;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public Money getAmount() {
		return amount;
	}

	public String getPgTransactionId() {
		return pgTransactionId;
	}

	public PaymentMethod getMethod() {
		return method;
	}

	public Payment withId(Long id) {
		Payment newPayment = Payment.builder()
			.paymentId(id)
			.userId(this.userId)
			.type(this.type)
			.status(this.status)
			.amount(this.amount)
			.pgTransactionId(this.pgTransactionId)
			.method(this.method)
			.build();

		newPayment.uncommittedHistory.addAll(this.uncommittedHistory);

		return newPayment;
	}

	@Override
	public String toString() {
		return "Payment{" +
			"id=" + getId() +
			", userId=" + userId +
			", type=" + type +
			", status=" + status +
			", amount=" + amount +
			'}';
	}

	// ========== Builder ==========

	public static class Builder {
		private Long paymentId;
		private String pgTransactionId;
		private Long userId;
		private PaymentType type;
		private PaymentStatus status;
		private Money amount;

		private PaymentMethod method;

		public Builder paymentId(Long paymentId) {
			this.paymentId = paymentId;
			return this;
		}

		public Builder userId(Long userId) {
			this.userId = userId;
			return this;
		}

		public Builder type(PaymentType type) {
			this.type = type;
			return this;
		}

		public Builder status(PaymentStatus status) {
			this.status = status;
			return this;
		}

		public Builder amount(Money amount) {
			this.amount = amount;
			return this;
		}

		public Builder pgTransactionId(String pgTransactionId) {
			this.pgTransactionId = pgTransactionId;
			return this;
		}

		public Builder method(PaymentMethod method) {
			this.method = method;
			return this;
		}

		public Payment build() {
			return new Payment(
				paymentId,
				userId,
				type,
				status,
				amount,
				pgTransactionId,
				method
			);
		}
	}
}
