package app.giftify.in;

import static org.springframework.transaction.event.TransactionPhase.*;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import app.giftify.app.funding.FundingSyncItemUseCase;
import app.giftify.in.event.WishlistItemCreatedEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WishlistEventConsumer {

	private final FundingSyncItemUseCase fundingSyncItemUseCase;

	@TransactionalEventListener(phase = AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleWishlistItemCreated(WishlistItemCreatedEvent event) {
		fundingSyncItemUseCase.syncItem(
			event.getWishlistItemId(),
			event.getProductId()
		);
	}
}

