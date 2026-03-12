package app.giftify.product.application.port.in;

import java.util.Map;

public interface DecreaseProductStockUseCase {
    // 주문에 의한 재고 감소 (펀딩/일반 주문 통합)
    void decreaseStockByOrder(Map<Long, Long> productQuantityMap);
}
