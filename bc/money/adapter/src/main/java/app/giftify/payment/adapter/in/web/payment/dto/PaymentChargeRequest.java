package app.giftify.payment.adapter.in.web.payment.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PaymentChargeRequest(
	@NotNull(message = "충전 금액은 필수입니다")
	@Min(value = 1000, message = "최소 충전 금액은 1,000원입니다")
	BigDecimal amount
) {
}