package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.support.common.money.Money;

public record CancelPaymentCommand(
	Long paymentId,
	Long requesterId,
	String reason,
	Money cancelAmount
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
	}

	public static CancelPaymentCommand full(Long paymentId, Long requesterId, String reason) {
		return new CancelPaymentCommand(paymentId, requesterId, reason, null);
	}

	public static CancelPaymentCommand withAmount(
		Long paymentId, Long requesterId, String reason, Money cancelAmount
	) {
		if (cancelAmount == null || !cancelAmount.isPositive()) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[CancelPaymentCommand] 취소 금액은 양수여야 합니다."
			);
		}
		return new CancelPaymentCommand(paymentId, requesterId, reason, cancelAmount);
	}
}
