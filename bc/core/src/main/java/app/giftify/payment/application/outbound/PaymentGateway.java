package app.giftify.payment.application.outbound;

import app.giftify.shared.domain.vo.Money;

/**
 * PG 에 대한 추상화
 */
public interface PaymentGateway {
	PgConfirmResult confirm(String paymentKey, String orderId, Money amount);
	PgCancelResult cancel(String paymentKey, String cancelReason, Money cancelAmount);
}
