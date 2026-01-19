package payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import app.giftify.shared.domain.event.payment.PaymentType;
import domain.payment.PaymentStatus;

public record PaymentDetailResponse(
        Long paymentId,
        PaymentType type,              // FUNDING (펀딩 참여 결제) , CHARGE (예치금 충전 결제)
        PaymentStatus status,           // PENDING / PAID / SETTLED / CANCELED / REFUNDED
        BigDecimal amount,
        Long payerId,
        LocalDateTime paidAt
) {
}
