package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;

public record CancelPaymentCommand(
	Long paymentId,
	Long requesterId,
	String reason
) {
	public CancelPaymentCommand {
		if (paymentId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[CancelPaymentCommand] paymentId는 필수입니다.");
		}
		if (requesterId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[CancelPaymentCommand] requesterId는 필수입니다.");
		}
		// reason은 optional - 검증하지 않음
	}
}
