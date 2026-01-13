package payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import domain.payment.PaymentStatus;
import domain.payment.PaymentType;

public record PaymentDetailResponse(
	Long paymentId,
	PaymentType type,              // GENERAL / FUNDING
	PaymentStatus status,           // COMPLETED / CANCELED
	BigDecimal amount,
	Long payerId,
	Long fundingId,                 // nullable
	LocalDateTime paidAt
) {
}
