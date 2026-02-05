package app.giftify.shared.domain.event.product;

import app.giftify.shared.domain.event.BaseDomainEvent;

// todo : 추후 재입고 알림 이벤트 등에 사용
public class ProductSaleEnabledEvent extends BaseDomainEvent {
    private final Long productId;

    public ProductSaleEnabledEvent(Long productId) {
        super();
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}
