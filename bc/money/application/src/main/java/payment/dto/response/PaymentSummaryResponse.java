package payment.dto.response;

import domain.payment.PaymentStatus;
import app.giftify.shared.domain.type.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentSummaryResponse(
        Long paymentId,
        PaymentType type,
        PaymentStatus status,
        BigDecimal amount,
        LocalDateTime paidAt
) {
}
