package app.giftify.payment.adapter.outbound.pg;

import org.springframework.stereotype.Component;

import app.giftify.payment.application.outbound.PaymentGateway;
import app.giftify.shared.domain.vo.Money;

@Component
public class TossPaymentsGatewayAdapter implements PaymentGateway {

	private final TossPaymentsClient tossPaymentsClient;

	public TossPaymentsGatewayAdapter(TossPaymentsClient tossPaymentsClient) {
		this.tossPaymentsClient = tossPaymentsClient;
	}

	@Override
	public TossConfirmResult confirm(String paymentKey, String orderId, Money amount) {
		return tossPaymentsClient.confirm(paymentKey, orderId, amount.amount());
	}

	@Override
	public TossCancelResult cancel(String paymentKey, String cancelReason, Money cancelAmount) {
		Long amountValue = cancelAmount != null ? cancelAmount.amount().longValue() : null;
		return tossPaymentsClient.cancelPayment(paymentKey, cancelReason, amountValue);
	}
}
