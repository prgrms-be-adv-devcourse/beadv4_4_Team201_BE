package app.giftify.payment.application.inbound;

import java.time.LocalDateTime;

import app.giftify.payment.domain.PaymentStatus;

/**
 * 결제 생성 결과.
 *
 * <p>{@code orderNumber}는 Toss SDK 호출 시 필요하며, 멱등성 키 역할도 겸합니다.</p>
 * <p>{@code paymentKey}와 {@code lastTransactionKey}는 PG사 연동 시에만 값이 존재합니다.
 * 예치금 결제 등 PG를 사용하지 않는 경우 null입니다.</p>
 */
public record PaymentCreatedResult(
	Long paymentId,
	String orderNumber,
	PaymentStatus status,
	String paymentKey,
	String lastTransactionKey,
	LocalDateTime createdAt
) {
}