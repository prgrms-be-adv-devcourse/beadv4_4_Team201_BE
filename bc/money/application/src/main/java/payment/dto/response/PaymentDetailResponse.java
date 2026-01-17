package payment.dto.response;

import domain.payment.PaymentStatus;
import app.giftify.shared.domain.event.payment.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
