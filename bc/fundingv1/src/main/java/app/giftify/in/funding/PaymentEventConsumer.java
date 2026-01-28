package app.giftify.in.funding;

import app.giftify.app.funding.FundingFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final FundingFacade fundingFacade;

    // TODO: PaymentCompletedEvent 구현 후 주석 해제


//    @TransactionalEventListener(phase = AFTER_COMMIT)
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void handlePaymentCompleted(PaymentCompletedEvent event) {
//        fundingFacade.startFunding(
//            new WishlistItemDto(
//            )
//        );
//    }
//
}
