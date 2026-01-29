package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;

public record PaymentDetailQuery(
	Long paymentId,
	Long requesterId
) {
	public PaymentDetailQuery {
		if (paymentId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[PaymentDetailQuery] paymentId는 필수입니다.");
		}
		if (requesterId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[PaymentDetailQuery] requesterId는 필수입니다.");
		}
	}
}
