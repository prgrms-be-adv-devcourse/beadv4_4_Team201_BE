package app.giftify.funding.adpater.inbound;

import app.giftify.funding.application.WithdrawFundingUseCase;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.type.TargetType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCanceledEventListener {
    private final WithdrawFundingUseCase withdrawFundingUseCase;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(OrderCanceledEvent event) {
        event.items().stream()
                .filter(item -> item.targetType() == TargetType.FUNDING)
                .forEach(item -> {
                    log.info(
                            "[Funding] 주문 취소 -> 펀딩 기여 철회. orderId={}, orderItemId={}, buyerId={}, targetId={}",
                            event.orderId(), item.orderItemId(), item.buyerId(), item.targetId()
                    );

                    withdrawFundingUseCase.withdrawByWishlistItem(
                            item.targetId(),      // wishlistItemId
                            item.buyerId(),
                            item.cancelAmount()
                    );
                });
    }
}
