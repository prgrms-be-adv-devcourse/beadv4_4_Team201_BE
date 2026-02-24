package app.giftify.payment.application.outbound;

import java.util.List;

import app.giftify.payment.domain.Cancel;

public interface CancelRepository {
	Cancel save(Cancel cancel);

	List<Cancel> findAllByPaymentId(Long paymentId);
}
