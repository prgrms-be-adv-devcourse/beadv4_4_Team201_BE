package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;

public record ReceivePaymentCommand(
	Long paymentId
) {
	public ReceivePaymentCommand {
		if (paymentId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ReceivePaymentCommand] paymentId는 필수입니다.");
		}
	}
}
