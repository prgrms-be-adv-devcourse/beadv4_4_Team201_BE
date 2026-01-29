package app.giftify.funding.in.funding;

import app.giftify.funding.app.funding.FundingFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WishlistEventConsumer {

    private final FundingFacade fundingFacade;

    // TODO: Member BC에 WishlistItemCreatedEvent 구현 후 주석 해제

//    @TransactionalEventListener(phase = AFTER_COMMIT)
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void handleWishlistItemCreated(WishlistItemCreatedEvent event) {
//        fundingFacade.syncItem(
//            event.getWishlistItemId(),
//            event.getProductId()
//        );
//    }

}
