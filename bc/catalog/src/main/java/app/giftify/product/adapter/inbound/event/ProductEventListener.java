package app.giftify.product.adapter.inbound.event;

import app.giftify.product.application.port.in.DecreaseProductStockUseCase;
import app.giftify.product.application.port.in.StockHistoryCreateUseCase;
import app.giftify.product.domain.event.ProductStockUpdatedEvent;
import app.giftify.shared.domain.event.order.OrderConfirmPendingEvent;
import app.giftify.shared.domain.vo.ConfirmItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {
    private final DecreaseProductStockUseCase decreaseProductStockUseCase;
    private final StockHistoryCreateUseCase stockHistoryCreateUseCase;

    // 주문의 이벤트를 받아 상품 재고 감소 (펀딩/일반 주문 통합)
    @EventListener
    public void handleOrderConfirmed(OrderConfirmPendingEvent event) {
        Map<Long, Long> productQuantityMap = event.getItems().stream()
                .collect(Collectors.toMap(ConfirmItem::productId, ConfirmItem::quantity));
        log.info("[product] 주문 확정 이벤트를 받았습니다. | 상품 수: {}", productQuantityMap.size());

        decreaseProductStockUseCase.decreaseStockByOrder(productQuantityMap);
        log.info("[product] 주문 재고 차감 완료 | 상품 수: {}", productQuantityMap.size());
    }

    // 재고 이력 생성
    @ApplicationModuleListener
    public void handleStockUpdated(ProductStockUpdatedEvent event) {
        Long productId = event.getProductId();
        log.info("[product] 상품 재고량 갱신 이벤트를 받았습니다.  | productId: {}", productId);

        stockHistoryCreateUseCase.createStockHistory(event.getResult());
        log.info("[product] 상품 재고 이력 저장 완료 | productId: {}", productId);
    }
}
