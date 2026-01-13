package payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import domain.payment.PaymentStatus;
import domain.payment.PaymentType;

public record PaymentSummaryResponse(
	Long paymentId,
	PaymentType type,
	PaymentStatus status,
	BigDecimal amount,
	LocalDateTime paidAt
) {
}
