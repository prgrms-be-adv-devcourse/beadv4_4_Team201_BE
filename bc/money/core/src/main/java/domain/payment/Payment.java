package domain.payment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.event.payment.PaymentType;
import app.giftify.shared.domain.vo.Money;

public class Payment extends BaseDomainModel {
	private static final String ORDER_UUID_PREFIX_CHARGE = "GFTFY_CHARGE_";
	private static final String ORDER_UUID_PREFIX_FUNDING = "GFTFY_FUNDING_"; // FIXME : Order 도메인에게 받아서 사용하도록 변경 필요

	private final Long userId;
	private final PaymentType type;
	private final String orderUuid;
	private PaymentStatus status;
	private final Money amount;
	private String paymentKey;
	private final PaymentMethod method;
	private final Money walletUsedAmount;  // 펀딩 복합결제 시 사용된 예치금 금액

	private final List<PaymentHistory> uncommittedHistory = new ArrayList<>();

	private Payment(
		Long id, Long userId, PaymentType type, PaymentStatus status,
		Money amount, String paymentKey, PaymentMethod method, String orderUuid,
		Money walletUsedAmount
	) {
		super(id);
		this.userId = userId;
		this.type = type;
		this.status = status;
		this.amount = amount;
		this.paymentKey = paymentKey;
		this.method = method;
		this.orderUuid = orderUuid;
		this.walletUsedAmount = walletUsedAmount;
	}

	private static String generateOrderUuid(PaymentType type) {
		String prefix = (type == PaymentType.CHARGE) ? ORDER_UUID_PREFIX_CHARGE : ORDER_UUID_PREFIX_FUNDING;
		String uuid = UUID.randomUUID().toString().replace("-", "");
		return prefix + uuid;
	}

	public static Builder builder() {
		return new Builder();
	}

	// ========== 정적 팩토리 메서드 ========== //

	/**
	 * 결제를 생성합니다. 초기 상태는 항상 PENDING 입니다.
	 * orderId는 자동으로 생성합니다
	 */
	public static Payment create(
		Long userId,
		PaymentType type,
		Money amount,
		PaymentMethod method
	) {
		LocalDateTime now = LocalDateTime.now();
		String orderUuid = generateOrderUuid(type);

		Payment payment = Payment.builder()
			.userId(userId)
			.type(type)
			.status(PaymentStatus.PENDING) // 항상 대기 상태로 시작
			.amount(amount)
			.method(method)
			.orderUuid(orderUuid)
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

	/**
	 * 예치금 전용 결제를 생성합니다.
	 * PG 결제 없이 예치금으로만 완납하는 경우 사용합니다.
	 * 초기 상태는 PENDING이며, 예치금 차감 후 markAsPaidByWallet()을 호출해야 합니다.
	 *
	 * <p>흐름: createGiftifyCashOnlyPayment → save → withdraw → markAsPaidByWallet → save</p>
	 */
	public static Payment createGiftifyCashOnlyPayment(
		Long userId,
		Money walletAmount,
		PaymentType type
	) {
		LocalDateTime now = LocalDateTime.now();
		String orderUuid = generateOrderUuid(type);

		Payment payment = Payment.builder()
			.userId(userId)
			.type(type)
			.status(PaymentStatus.PENDING)  // PENDING으로 시작, 예치금 차감 후 PAID로 전환
			.amount(Money.zero())           // PG 결제액 없음
			.walletUsedAmount(walletAmount)
			.orderUuid(orderUuid)
			.build();

		payment.uncommittedHistory.add(new PaymentHistory(
			null,
			null,
			PaymentEventType.CREATED,
			now,
			"{\"walletOnly\":true}"
		));

		return payment;
	}

	/**
	 * 펀딩 복합결제용 결제를 생성합니다.
	 * 예치금 사용액(walletUsedAmount)을 함께 저장하여 PG 결제 실패 시 롤백에 활용합니다.
	 */
	public static Payment createForFunding(
		Long userId,
		Money pgAmount,
		Money walletUsedAmount
	) {
		LocalDateTime now = LocalDateTime.now();
		String orderUuid = generateOrderUuid(PaymentType.FUNDING);

		Payment payment = Payment.builder()
			.userId(userId)
			.type(PaymentType.FUNDING)
			.status(PaymentStatus.PENDING)
			.amount(pgAmount)
			.walletUsedAmount(walletUsedAmount)
			.orderUuid(orderUuid)
			.build();

		payment.uncommittedHistory.add(new PaymentHistory(
			null,
			null,
			PaymentEventType.CREATED,
			now,
			null
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
		this.paymentKey = pgTransactionId;

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

	/**
	 * 예치금 전용 결제를 완료 처리
	 * PG 결제 없이 예치금으로만 완납된 경우 사용합니다.
	 *
	 * @throws PaymentException 상태가 PENDING이 아닌 경우
	 */
	public PaymentHistory markAsPaidByWallet() {
		if (this.status != PaymentStatus.PENDING) {
			throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS,
				"[Payment] 결제 대기(PENDING) 상태에서만 완료 처리할 수 있습니다. 현재 상태: " + this.status);
		}

		LocalDateTime now = LocalDateTime.now();
		this.status = PaymentStatus.PAID;
		// paymentKey는 null (PG 거래 없음)
		PaymentHistory history = new PaymentHistory(
			getId(),
			null,  // 예치금 결제이므로 paymentKey 없음
			PaymentEventType.PAID,
			now,
			"{\"walletOnly\":true}"
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

	public String getPaymentKey() {
		return paymentKey;
	}

	public PaymentMethod getMethod() {
		return method;
	}

	public String getOrderUuid() {
		return orderUuid;
	}

	public Money getWalletUsedAmount() {
		return walletUsedAmount;
	}

	public Payment withId(Long id) {
		Payment newPayment = Payment.builder()
			.paymentId(id)
			.userId(this.userId)
			.type(this.type)
			.status(this.status)
			.amount(this.amount)
			.paymentKey(this.paymentKey)
			.method(this.method)
			.orderUuid(this.orderUuid)
			.walletUsedAmount(this.walletUsedAmount)
			.build();

		newPayment.uncommittedHistory.addAll(this.uncommittedHistory);

		return newPayment;
	}

	@Override
	public String toString() {
		return "Payment{" +
			"id=" + getId() +
			", orderUuid='" + orderUuid + '\'' +
			", userId=" + userId +
			", type=" + type +
			", status=" + status +
			", amount=" + amount +
			'}';
	}

	@Override
	public final boolean equals(Object object) {
		if (!(object instanceof Payment payment))
			return false;
		if (!super.equals(object))
			return false;

		return Objects.equals(userId, payment.userId) && type == payment.type && Objects.equals(orderUuid,
			payment.orderUuid) && status == payment.status && Objects.equals(amount, payment.amount)
			&& Objects.equals(paymentKey, payment.paymentKey) && method == payment.method
			&& Objects.equals(walletUsedAmount, payment.walletUsedAmount) && uncommittedHistory.equals(
			payment.uncommittedHistory);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + Objects.hashCode(userId);
		result = 31 * result + Objects.hashCode(type);
		result = 31 * result + Objects.hashCode(orderUuid);
		result = 31 * result + Objects.hashCode(status);
		result = 31 * result + Objects.hashCode(amount);
		result = 31 * result + Objects.hashCode(paymentKey);
		result = 31 * result + Objects.hashCode(method);
		result = 31 * result + Objects.hashCode(walletUsedAmount);
		result = 31 * result + uncommittedHistory.hashCode();
		return result;
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
		private String orderUuid;
		private Money walletUsedAmount;

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

		public Builder paymentKey(String pgTransactionId) {
			this.pgTransactionId = pgTransactionId;
			return this;
		}

		public Builder method(PaymentMethod method) {
			this.method = method;
			return this;
		}

		public Builder orderUuid(String orderUuid) {
			this.orderUuid = orderUuid;
			return this;
		}

		public Builder walletUsedAmount(Money walletUsedAmount) {
			this.walletUsedAmount = walletUsedAmount;
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
				method,
				orderUuid,
				walletUsedAmount
			);
		}
	}
}
