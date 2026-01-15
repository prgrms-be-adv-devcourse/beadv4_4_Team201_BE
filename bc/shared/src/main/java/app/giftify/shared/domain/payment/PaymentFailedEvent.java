package app.giftify.shared.domain.payment;

import java.time.LocalDateTime;

import app.giftify.shared.domain.vo.Money;

public record PaymentFailedEvent(
	Long paymentId, Long userId,
	Money amount,
	PaymentType type,  // 어떤 결제였는지 식별
	String reason, LocalDateTime occurredAt
) {
}
