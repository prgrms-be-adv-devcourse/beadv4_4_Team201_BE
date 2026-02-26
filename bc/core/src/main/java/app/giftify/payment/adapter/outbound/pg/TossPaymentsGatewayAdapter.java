package app.giftify.payment.adapter.outbound.pg;

import org.springframework.stereotype.Component;

import app.giftify.payment.application.outbound.PaymentGateway;
import app.giftify.payment.application.outbound.PgCancelResult;
import app.giftify.payment.application.outbound.PgConfirmResult;
import app.giftify.shared.domain.vo.Money;

@Component
public class TossPaymentsGatewayAdapter implements PaymentGateway {

	private final TossPaymentsClient tossPaymentsClient;

	public TossPaymentsGatewayAdapter(TossPaymentsClient tossPaymentsClient) {
		this.tossPaymentsClient = tossPaymentsClient;
	}

	@Override
	public PgConfirmResult confirm(String paymentKey, String orderId, Money amount) {
		TossConfirmResult toss = tossPaymentsClient.confirm(paymentKey, orderId, amount.amount());
		return toss.success()
			? PgConfirmResult.success(toss.paymentKey(), toss.lastTransactionKey(), toss.approveNo())
			: PgConfirmResult.failure(toss.errorCode(), toss.errorMessage());
	}

	@Override
	public PgCancelResult cancel(String paymentKey, String cancelReason, Money cancelAmount) {
		Long amountValue = cancelAmount != null ? cancelAmount.amount().longValue() : null;
		TossCancelResult toss = tossPaymentsClient.cancelPayment(paymentKey, cancelReason, amountValue);
		if (!toss.success()) {
			return PgCancelResult.failure(toss.errorCode(), toss.errorMessage());
		}
		var cancels = toss.cancels().stream()
			.map(c -> new PgCancelResult.CancelDetail(c.transactionKey(), c.cancelAmount(), c.canceledAt()))
			.toList();
		return PgCancelResult.success(toss.paymentKey(), toss.lastTransactionKey(), cancels);
	}
}
