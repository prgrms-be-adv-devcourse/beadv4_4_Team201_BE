package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

public record ConfirmPaymentCommand(
	Long memberId,
	String orderId,
	PaymentType type,
	PaymentMethod method,
	Money amount
) {
	public ConfirmPaymentCommand {
		if (memberId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ConfirmPaymentCommand] memberId는 필수입니다.");
		}
		if (orderId == null || orderId.isBlank()) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ConfirmPaymentCommand] orderId는 필수입니다.");
		}
		if (type == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ConfirmPaymentCommand] type은 필수입니다.");
		}
		if (method == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ConfirmPaymentCommand] method는 필수입니다.");
		}
		if (amount == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[ConfirmPaymentCommand] amount는 필수입니다.");
		}
	}
}
