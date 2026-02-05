package app.giftify.shared.domain.event.product;

import app.giftify.shared.domain.event.BaseDomainEvent;

// todo 품절 발생 -> 진행 중인 펀딩에 이벤트를 발행해야 하는 경우에 사용
public class ProductSaleDisabledEvent extends BaseDomainEvent {
    private final Long productId;

    public ProductSaleDisabledEvent(Long productId) {
        super();
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}
