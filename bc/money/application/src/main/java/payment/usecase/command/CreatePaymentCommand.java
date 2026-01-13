package payment.usecase.command;

import java.math.BigDecimal;

public record CreatePaymentCommand(
	Long payerId,
	Long orderId,
	BigDecimal amount
	// DeliveryOption deliveryOption
) {
}
