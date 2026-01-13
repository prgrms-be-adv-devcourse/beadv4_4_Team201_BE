package payment.usecase.command;

import java.math.BigDecimal;

public record RefundPaymentCommand(
	Long paymentId,
	BigDecimal refundAmount
) {
}
