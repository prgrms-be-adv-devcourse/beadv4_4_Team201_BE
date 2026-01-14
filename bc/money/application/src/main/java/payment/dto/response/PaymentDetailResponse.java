package payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import domain.payment.PaymentStatus;
import domain.payment.PaymentType;

public record PaymentDetailResponse(
	Long paymentId,
	PaymentType type,              // FUNDING (펀딩 참여 결제)
	PaymentStatus status,           // PENDING / PAID / SETTLED / CANCELED / REFUNDED
	BigDecimal amount,
	Long payerId,
	Long fundingId,                 // nullable (펀딩 결제일 경우에만)
	LocalDateTime paidAt
) {
}
