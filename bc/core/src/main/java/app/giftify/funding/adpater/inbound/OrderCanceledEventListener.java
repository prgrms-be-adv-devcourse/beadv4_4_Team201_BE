package app.giftify.funding.adpater.inbound;

import app.giftify.funding.application.WithdrawFundingUseCase;
import app.giftify.shared.domain.event.order.OrderCanceledEvent;
import app.giftify.shared.domain.type.TargetType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCanceledEventListener {
    private final WithdrawFundingUseCase withdrawFundingUseCase;

    @ApplicationModuleListener
    public void handle(OrderCanceledEvent event) {
        event.getItems().stream()
                .filter(item -> item.targetType() == TargetType.FUNDING)
                .forEach(item -> {
                    log.info(
                            "[Funding] 주문 취소 -> 펀딩 기여 철회. orderId={}, orderItemId={}, buyerId={}, targetId={}",
                            event.getOrderId(), item.orderItemId(), item.buyerId(), item.targetId()
                    );

                    withdrawFundingUseCase.withdrawByWishlistItem(
                            item.targetId(),      // wishlistItemId
                            item.buyerId(),
                            item.cancelAmount()
                    );
                });
    }
}
