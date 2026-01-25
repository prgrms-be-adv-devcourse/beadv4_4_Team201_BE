package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;

public record ReceivePaymentCommand(
	Long paymentId,
	Long requesterId
) {
	public ReceivePaymentCommand {
		if (paymentId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ReceivePaymentCommand] paymentId는 필수입니다.");
		}
		if (requesterId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ReceivePaymentCommand] requesterId는 필수입니다.");
		}
	}
}
