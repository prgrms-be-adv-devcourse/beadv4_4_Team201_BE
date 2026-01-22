package app.giftify.payment.adapter.in.web.payment.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentConfirmRequest(
	@NotBlank(message = "paymentKey는 필수입니다")
	String paymentKey,

	@NotNull(message = "paymentId는 필수입니다")
	Long paymentId,

	@NotNull(message = "금액은 필수입니다")
	@Positive(message = "금액은 0보다 커야 합니다")
	BigDecimal amount
) {
}