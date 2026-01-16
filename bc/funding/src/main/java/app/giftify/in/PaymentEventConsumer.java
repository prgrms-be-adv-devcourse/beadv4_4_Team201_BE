package app.giftify.in;

import static org.springframework.transaction.event.TransactionPhase.*;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import app.giftify.app.funding.FundingFacade;
import app.giftify.in.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

	private final FundingFacade fundingFacade;

	@TransactionalEventListener(phase = AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handlePaymentCompleted(PaymentCompletedEvent event) {
		fundingFacade.createFunding(
			event.getPayerId(),
			event.getWishlistItemId(),
			event.getAmount()
		);
	}
}
