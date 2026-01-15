package in;

import app.funding.FundingSyncItemUseCase;
import in.event.WishlistItemCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

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

