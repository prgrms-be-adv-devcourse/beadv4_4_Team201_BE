package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.shared.domain.vo.Money;

public record RefundPaymentCommand(
	Long paymentId,
	Long requesterId,
	String reason,
	Money refundAmount
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
		if (refundAmount == null || refundAmount.isLessThanOrEqual(Money.zero())) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[RefundPaymentCommand] refundAmount는 양수여야 합니다.");
		}
	}
}
