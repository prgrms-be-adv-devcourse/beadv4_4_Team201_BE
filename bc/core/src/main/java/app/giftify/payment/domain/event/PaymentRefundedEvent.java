package app.giftify.payment.domain.event;

import java.time.LocalDateTime;

import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

/**
 * 결제 환불 내부 이벤트.
 * PaymentInternalEventHandler에서 외부 이벤트로 변환됩니다.
 */
public final class PaymentRefundedEvent extends PaymentInternalEvent {
	private final Long memberId;
	private final String orderId;
	private final PaymentType paymentType;
	private final Money refundAmount;
	private final String reason;
	private final LocalDateTime refundedAt;

	public PaymentRefundedEvent(
		Long paymentId,
		Long memberId,
		String orderId,
		PaymentType paymentType,
		Money refundAmount,
		String reason,
		LocalDateTime refundedAt
	) {
		super(paymentId);
		this.memberId = memberId;
		this.orderId = orderId;
		this.paymentType = paymentType;
		this.refundAmount = refundAmount;
		this.reason = reason;
		this.refundedAt = refundedAt;
	}

	public Long getMemberId() {
		return memberId;
	}

	public String getOrderId() {
		return orderId;
	}

	public PaymentType getPaymentType() {
		return paymentType;
	}

	public Money getRefundAmount() {
		return refundAmount;
	}

	public String getReason() {
		return reason;
	}

	public LocalDateTime getRefundedAt() {
		return refundedAt;
	}
}
