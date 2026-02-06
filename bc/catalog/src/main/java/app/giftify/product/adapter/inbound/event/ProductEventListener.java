package app.giftify.product.adapter.inbound.event;

import app.giftify.product.application.port.in.DecreaseProductStockUseCase;
import app.giftify.shared.domain.event.funding.FundingAcceptedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {
    private final DecreaseProductStockUseCase decreaseProductStockUseCase;

    // 펀딩 수락 시 재고 감소
    @EventListener
    public void handleFundingAccepted(FundingAcceptedEvent event) {
        Long productId = event.getProductId();
        log.info("[product] 펀딩 수락 이벤트를 받았습니다.  | productId: {}", productId);

        decreaseProductStockUseCase.decreaseStockByFunding(productId);
        log.info("[product] 상품 재고 차감 완료 | productId: {}", productId);

        // todo 재고 이력 저장 이벤트 호출 new event(result) - AFTER_COMMIT
    }
}
