package app.giftify.in.funding;

import app.giftify.app.funding.FundingSyncItemUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WishlistEventConsumer {

    private final FundingSyncItemUseCase fundingSyncItemUseCase;

    // TODO: Member BC에 WishlistItemCreatedEvent 구현 후 주석 해제
    /*
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleWishlistItemCreated(WishlistItemCreatedEvent event) {
        fundingSyncItemUseCase.syncItem(
            event.getWishlistItemId(),
            event.getProductId()
        );
    }
    */
}

