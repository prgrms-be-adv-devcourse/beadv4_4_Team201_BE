package app.giftify.payment.adapter.in.web.payment.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 결제 시작 요청 DTO.
 * Order BC에서 생성한 주문 ID와 결제 금액을 전달합니다.
 */
public record PaymentInitiateRequest(
	@NotNull(message = "주문 ID는 필수입니다")
	Long orderId,

	@NotNull(message = "결제 금액은 필수입니다")
	@Min(value = 1000, message = "최소 결제 금액은 1,000원입니다")
	BigDecimal amount
) {
}