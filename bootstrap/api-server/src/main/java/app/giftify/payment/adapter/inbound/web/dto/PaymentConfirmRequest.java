package app.giftify.payment.adapter.inbound.web.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentConfirmRequest(
	@NotNull Long paymentId,
	@NotNull String paymentKey,
	@NotNull String orderId,
	@NotNull BigDecimal amount
) {}
