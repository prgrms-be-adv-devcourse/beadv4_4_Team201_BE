package app.giftify.product.application.port.in;

import app.giftify.shared.domain.vo.SellerOrderItem;

import java.util.List;
import java.util.Map;

public interface DecreaseProductStockUseCase {
    // 주문에 의한 재고 감소 (펀딩/일반 주문 통합)
    List<SellerOrderItem> decreaseStockByOrder(Map<Long, Integer> productQuantityMap);
}
