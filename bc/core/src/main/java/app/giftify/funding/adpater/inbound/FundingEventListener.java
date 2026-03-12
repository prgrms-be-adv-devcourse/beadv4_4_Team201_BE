package app.giftify.funding.adpater.inbound;

import app.giftify.funding.application.FundingAcceptUseCase;
import app.giftify.funding.application.SyncFundingProductUseCase;
import app.giftify.funding.application.WithdrawFundingUseCase;
import app.giftify.shared.domain.event.order.OrderCanceledEvent;
import app.giftify.shared.domain.event.order.OrderConfirmedEvent;
import app.giftify.shared.domain.event.product.ProductUpdatedEvent;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.support.common.annotation.EventIdempotent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FundingEventListener {
	private final WithdrawFundingUseCase withdrawFundingUseCase;
    private final FundingAcceptUseCase fundingAcceptUseCase;
    private final SyncFundingProductUseCase syncFundingProductUseCase;


	@ApplicationModuleListener
	@EventIdempotent(prefix = "FUNDING_WITHDRAW")
	public void handleOrderCanceled(OrderCanceledEvent event) {
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

    @ApplicationModuleListener
    public void handleConfirmFunding(OrderConfirmedEvent event) {
        fundingAcceptUseCase.confirmFundingAcceptance(event.getFundingId());
    }

    @ApplicationModuleListener
    public void handleProductUpdated(ProductUpdatedEvent event) {
        syncFundingProductUseCase.syncFundingProduct(event.getProductId(), event.getProductPrice(), event.getProductName(), event.getImageKey());
    }
}