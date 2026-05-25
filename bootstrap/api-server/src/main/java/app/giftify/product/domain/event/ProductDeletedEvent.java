package app.giftify.product.domain.event;

import app.giftify.support.common.event.BaseDomainEvent;

public class ProductDeletedEvent extends BaseDomainEvent {
    private final Long productId;

    public ProductDeletedEvent(Long productId) {
        super();
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }

    @Override
    public String toString() {
        return "ProductDeletedEvent{" +
                "productId=" + productId + "}";
    }
}
