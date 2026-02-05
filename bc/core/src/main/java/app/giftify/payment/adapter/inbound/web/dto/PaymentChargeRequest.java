package app.giftify.payment.adapter.inbound.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 포인트 충전 요청 DTO.
 *
 * <p>{@code orderId}는 멱등성 키 역할을 합니다.
 * 같은 orderId로 중복 요청 시 기존 결제가 반환됩니다.</p>
 */
public record PaymentChargeRequest(
	@NotNull @Positive BigDecimal amount,
	@NotBlank String orderId,    // 필수 - 멱등성 키 역할
	String paymentType
) {}
