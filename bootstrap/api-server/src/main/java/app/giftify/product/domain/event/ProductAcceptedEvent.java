package app.giftify.product.domain.event;

import app.giftify.support.common.event.BaseDomainEvent;
import lombok.Getter;

@Getter
public class ProductAcceptedEvent extends BaseDomainEvent {
    private final Long productId;

    public ProductAcceptedEvent(Long productId) {
        super();
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "ProductAcceptedEvent{" +
                "productId=" + productId + "}";
    }
}
