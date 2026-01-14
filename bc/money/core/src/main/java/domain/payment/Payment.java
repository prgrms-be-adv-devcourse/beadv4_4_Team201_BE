package domain.payment;

import java.time.LocalDateTime;

import vo.Money;

public class Payment {
	private Long paymentId;
	private Long userId;
	private PaymentType type;
	private PaymentStatus status;
	private Money amount;
	private Long fundingId;             // nullable (펀딩 결제일 경우에만 활성화)
	private String pgTransactionId; // PG사 거래 ID
	private PaymentMethod method;
	private LocalDateTime createdAt;
	private LocalDateTime paidAt;
	private LocalDateTime refundedAt;
	private LocalDateTime settledAt;

	private Payment(
		Long userId,
		PaymentType type, PaymentStatus status,
		Money amount,
		Long fundingId,
		PaymentMethod method,
		LocalDateTime paidAt
	) {
		// 펀딩 결제일 경우 fundingId 필수
		if (type == PaymentType.FUNDING && fundingId == null) {
			throw new IllegalArgumentException("펀딩 결제는 fundingId가 필수입니다.");
		}

		this.userId = userId;
		this.type = type;
		this.status = status;
		this.amount = amount;
		this.fundingId = fundingId;
		this.method = method;
		this.createdAt = LocalDateTime.now();
		this.paidAt = paidAt;            // 외부에서 주입 (null or now)
	}

	public static Payment createPaidForFunding(Long userId, Long fundingId, Money amount) {
		return new Payment(
			userId,
			PaymentType.FUNDING,
			PaymentStatus.PAID,
			amount,
			fundingId,
			PaymentMethod.GIFTIFY_CASH,
			LocalDateTime.now()
		);
	}

	public void settle() {
		if (this.status != PaymentStatus.PAID) {
			throw new IllegalStateException("결제 완료(PAID) 상태에서만 확정할 수 있습니다.");
		}
		this.status = PaymentStatus.SETTLED;
		this.settledAt = LocalDateTime.now();
	}

	public void refund() {
		if (this.status == PaymentStatus.SETTLED) {
			throw new IllegalStateException("이미 수령 처리되어 환불할 수 없습니다.");
		}
		if (!this.status.canRefund()) {
			throw new IllegalStateException("환불 불가능한 상태입니다: " + this.status);
		}
		this.status = PaymentStatus.REFUNDED;
		this.refundedAt = LocalDateTime.now();
	}

	public void cancel() {
		if (!this.status.canCancel()) {
			throw new IllegalStateException("취소 불가능한 상태입니다: " + this.status);
		}
		this.status = PaymentStatus.CANCELED;
	}

	public void markAsPaid(String pgTransactionId) {
		if (this.status != PaymentStatus.PENDING) {
			throw new IllegalStateException("결제 대기(PENDING) 상태에서만 완료 처리할 수 있습니다.");
		}
		this.status = PaymentStatus.PAID;
		this.pgTransactionId = pgTransactionId;
		this.paidAt = LocalDateTime.now();
	}

	public boolean isRefundable() {
		return this.status == PaymentStatus.PAID && this.refundedAt == null && this.settledAt == null;
	}

	public boolean isCancelable() {
		return this.status == PaymentStatus.PENDING;
	}
}
