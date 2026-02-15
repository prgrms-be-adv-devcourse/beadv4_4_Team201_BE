package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

public record InternalPaymentResult(
	Long paymentId,
	String orderNumber,
	Long memberId,
	PaymentStatus status,
	PaymentType type,
	PaymentMethod method,
	Money originAmount,
	Money paidAmount,
	String paymentKey,
	String approveCode
) {

	public static InternalPaymentResult of(
		Payment payment,
		String decryptedPaymentKey,
		String decryptedApproveCode
	) {
		return new InternalPaymentResult(
			payment.getId(),
			payment.getOrderNumber(),
			payment.getMemberId(),
			payment.getStatus(),
			payment.getType(),
			payment.getMethod(),
			payment.getOriginAmount(),
			payment.getPaidAmount(),
			decryptedPaymentKey,
			decryptedApproveCode
		);
	}
}
