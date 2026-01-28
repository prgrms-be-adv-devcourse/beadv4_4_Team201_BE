package app.giftify.payment.application.outbound;

import app.giftify.payment.adapter.outbound.pg.TossCancelResult;
import app.giftify.payment.adapter.outbound.pg.TossConfirmResult;
import app.giftify.shared.domain.vo.Money;

/**
 * PG 에 대한 추상화
 */
public interface PaymentGateway {
	TossConfirmResult confirm(String paymentKey, String orderId, Money amount);
	TossCancelResult cancel(String paymentKey, String cancelReason);
}
