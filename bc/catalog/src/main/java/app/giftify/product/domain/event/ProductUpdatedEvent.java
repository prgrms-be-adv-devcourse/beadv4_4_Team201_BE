package app.giftify.product.domain.event;

import app.giftify.shared.domain.event.BaseDomainEvent;
import lombok.Getter;

@Getter
public class ProductUpdatedEvent extends BaseDomainEvent {
    private final Long productId;

    public ProductUpdatedEvent(Long productId) {
        super();
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "ProductUpdatedEvent{" +
                "productId=" + productId + "}";
    }
}
