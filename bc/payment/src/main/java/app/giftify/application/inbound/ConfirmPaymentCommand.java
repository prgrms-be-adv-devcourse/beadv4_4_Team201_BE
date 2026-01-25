package app.giftify.application.inbound;

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
}
