package payment.usecase.command;

import java.math.BigDecimal;

import app.giftify.shared.domain.payment.PaymentType;

public record CreatePaymentCommand(
	Long payerId,
    Long receiverId,
    Long productId,
    BigDecimal amount,
    PaymentType paymentType
) {
}
