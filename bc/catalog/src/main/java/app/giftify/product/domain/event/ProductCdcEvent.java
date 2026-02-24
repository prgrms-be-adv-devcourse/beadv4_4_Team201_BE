package app.giftify.product.domain.event;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class ProductCdcEvent extends BaseDomainEvent {
    private final Long productId;

    public ProductCdcEvent(Long productId) {
        super();
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "ProductCdcEvent{" +
                "productId=" + productId + "}";
    }

    public Long getProductId() {
        return productId;
    }
}
