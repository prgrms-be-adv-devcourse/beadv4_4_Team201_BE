package app.giftify.payment.application.inbound;

import java.util.List;
import java.util.Map;

import app.giftify.support.common.money.Money;

public interface BulkPaymentAmountUseCase {
	Map<Long, Money> getBulkAmounts(List<Long> orderIds);
}
