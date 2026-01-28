package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;

public record RefundPaymentCommand(
	Long paymentId,
	Long requesterId,
	String reason
) {
	public RefundPaymentCommand {
		if (paymentId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[RefundPaymentCommand] paymentId는 필수입니다.");
		}
		if (requesterId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[RefundPaymentCommand] requesterId는 필수입니다.");
		}
	}
}
