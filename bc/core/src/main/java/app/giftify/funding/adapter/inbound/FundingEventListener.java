package app.giftify.funding.adapter.inbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.funding.application.FundingAcceptUseCase;
import app.giftify.funding.application.FundingFacade;
import app.giftify.funding.application.FundingFailAcceptUseCase;
import app.giftify.funding.application.SyncFundingProductUseCase;
import app.giftify.funding.application.WithdrawFundingUseCase;
import app.giftify.order.application.OrderService;
import app.giftify.order.domain.OrderSnapshot;
import app.giftify.shared.api.exception.InfraException;
import app.giftify.shared.domain.event.order.OrderCanceledEvent;
import app.giftify.shared.domain.event.order.OrderConfirmFailedEvent;
import app.giftify.shared.domain.event.order.OrderConfirmedEvent;
import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import app.giftify.shared.domain.event.product.ProductUpdatedEvent;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.support.common.annotation.EventIdempotent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FundingEventListener {
	private static final Logger log = LoggerFactory.getLogger(FundingEventListener.class);

	private final WithdrawFundingUseCase withdrawFundingUseCase;
    private final FundingAcceptUseCase fundingAcceptUseCase;
    private final SyncFundingProductUseCase syncFundingProductUseCase;
    private final FundingFailAcceptUseCase fundingFailAcceptUseCase;
    private final OrderService orderService;
    private final FundingFacade fundingFacade;


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

    @EventListener
    public void handleConfirmFunding(OrderConfirmedEvent event) {
        fundingAcceptUseCase.confirmFundingAcceptance(event.getFundingId());
    }

    @ApplicationModuleListener
    public void handleProductUpdated(ProductUpdatedEvent event) {
        syncFundingProductUseCase.syncFundingProduct(event.getProductId(), event.getProductPrice(), event.getProductName(), event.getImageKey());
    }

    @EventListener
    public void handleOrderConfirmFail(OrderConfirmFailedEvent event) {
        fundingFailAcceptUseCase.execute(event.getFundingId());
    }

    @Retryable(
            retryFor = InfraException.class,
            exceptionExpression = "#root.errorCode.isRetryable",
            backoff = @Backoff(delay = 100, multiplier = 2.0, random = true)
    )
    @ApplicationModuleListener
    public void on(PaymentSucceededEvent event) {
        log.info("[이벤트 수신] 결제 성공 -> 펀딩 생성/기여 처리 시작. OrderId: {}", event.data().orderId());
        OrderSnapshot snapshot = orderService.getSnapshotByOrderNumber(event.data().orderNumber());
        fundingFacade.processFundingActions(snapshot);
    }

    @Recover
    public void recover(InfraException e, PaymentSucceededEvent event) {
        log.error("================================================================");
        log.error("[최종 장애] 펀딩 생성/기여 처리 최종 실패 (시스템 자동 복구 불가)");
        log.error("Order ID: {}", event.data().orderId());
        log.error("Order Number: {}", event.data().orderNumber());
        log.error("Event ID: {}", event.id());
        log.error("Reason: {}", e.getMessage());
        log.error("조치 사항: DB 상태 확인 후 수동 정정 필요");
        log.error("================================================================");
        throw e;
    }
}