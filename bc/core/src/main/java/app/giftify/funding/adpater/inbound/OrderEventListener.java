package app.giftify.funding.adpater.inbound;

import app.giftify.funding.application.FundingFacade;
import app.giftify.funding.application.FundingFromOrderCommand;
import app.giftify.shared.domain.type.TargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderEventListener {
    private final FundingFacade fundingFacade;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)  // 주문이 롤백되면 펀딩 처리도 롤백되도록 하려면 이걸 지워야되지 않나
    public void handleOrderItemOrdered(OrderItemOrderedEvent event) {
        // 펀딩 대상이 아니면 무시
        if (event.targetYpe() != TargetType.FUNDING) {
            return;
        }

        // 펀딩 처리
        fundingFacade.handleFundingFromOrder(
                new FundingFromOrderCommand(
                        event.wishlistItemId(),
                        event.receiverId(),
                        event.productName(),
                        event.amount()
                )
        );
    }
}
