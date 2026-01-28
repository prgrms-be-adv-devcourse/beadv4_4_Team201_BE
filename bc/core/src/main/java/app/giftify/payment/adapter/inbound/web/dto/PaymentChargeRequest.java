package app.giftify.payment.adapter.inbound.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PaymentChargeRequest(
	@NotNull @Positive BigDecimal amount,
	String orderId,
	String paymentType
) {}
