package app.giftify.payment.domain.event;

import java.time.LocalDateTime;

import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

/**
 * 결제 완료 내부 이벤트.
 * PaymentInternalEventHandler에서 외부 이벤트로 변환됩니다.
 */
public final class PaymentPaidEvent extends PaymentInternalEvent {
	private final Long memberId;
	private final String orderId;
	private final PaymentType paymentType;
	private final Money paidAmount;
	private final LocalDateTime paidAt;

	public PaymentPaidEvent(
		Long paymentId,
		Long memberId,
		String orderId,
		PaymentType paymentType,
		Money paidAmount,
		LocalDateTime paidAt
	) {
		super(paymentId);
		this.memberId = memberId;
		this.orderId = orderId;
		this.paymentType = paymentType;
		this.paidAmount = paidAmount;
		this.paidAt = paidAt;
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

	public LocalDateTime getPaidAt() {
		return paidAt;
	}
}
