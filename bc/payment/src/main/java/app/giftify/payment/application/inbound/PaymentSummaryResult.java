package app.giftify.payment.application.inbound;

import java.time.LocalDateTime;

import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.vo.Money;

public record PaymentSummaryResult(
	Long paymentId,
	String orderId,
	Money paidAmount,
	PaymentStatus status,
	LocalDateTime createdAt
) {
	/**
	 * Payment 도메인 객체로부터 PaymentSummaryResult를 생성
	 *
	 * @param payment Payment 도메인 객체
	 * @return PaymentSummaryResult 인스턴스
	 */
	public static PaymentSummaryResult from(Payment payment) {
		return new PaymentSummaryResult(
			payment.getId(),
			payment.getOrderId(),
			payment.getPaidAmount(),
			payment.getStatus(),
			payment.getPaidAt()
		);
	}
}
