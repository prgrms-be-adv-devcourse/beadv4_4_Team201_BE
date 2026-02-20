package app.giftify.payment.domain.event;

import java.time.LocalDateTime;

import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

/**
 * 결제 완료 내부 이벤트.
 * PaymentInternalEventHandler에서 외부 이벤트로 변환됩니다.
 */
public final class PaymentConfirmedEvent extends PaymentInternalEvent {
	private final Long memberId;
	private final String orderNumber;
	private final PaymentType paymentType;
	private final Money paidAmount;
	private final LocalDateTime paidAt;

	public PaymentConfirmedEvent(
		Long paymentId,
		Long memberId,
		String orderNumber,
		PaymentType paymentType,
		Money paidAmount,
		LocalDateTime paidAt
	) {
		super(paymentId);
		this.memberId = memberId;
		this.orderNumber = orderNumber;
		this.paymentType = paymentType;
		this.paidAmount = paidAmount;
		this.paidAt = paidAt;
	}

	public Long getMemberId() {
		return memberId;
	}

	public String getOrderNumber() {
		return orderNumber;
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
