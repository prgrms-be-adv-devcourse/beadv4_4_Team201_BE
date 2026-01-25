package app.giftify.payment.domain.event;

import java.time.LocalDateTime;

import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

/**
 * 결제 취소 내부 이벤트.
 * PaymentInternalEventHandler에서 외부 이벤트로 변환됩니다.
 */
public final class PaymentCanceledEvent extends PaymentInternalEvent {
	private final Long memberId;
	private final String orderId;
	private final PaymentType paymentType;
	private final Money paidAmount;
	private final String reason;
	private final LocalDateTime canceledAt;

	public PaymentCanceledEvent(
		Long paymentId,
		Long memberId,
		String orderId,
		PaymentType paymentType,
		Money paidAmount,
		String reason,
		LocalDateTime canceledAt
	) {
		super(paymentId);
		this.memberId = memberId;
		this.orderId = orderId;
		this.paymentType = paymentType;
		this.paidAmount = paidAmount;
		this.reason = reason;
		this.canceledAt = canceledAt;
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

	public Money getPaidAmount() {
		return paidAmount;
	}

	public String getReason() {
		return reason;
	}

	public LocalDateTime getCanceledAt() {
		return canceledAt;
	}
}
