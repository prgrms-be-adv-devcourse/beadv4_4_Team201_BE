package app.giftify.product.domain.event;

import app.giftify.product.domain.StockChangeResult;
import app.giftify.support.common.event.BaseDomainEvent;

public class ProductStockUpdatedEvent extends BaseDomainEvent {
    private final StockChangeResult result;

    public ProductStockUpdatedEvent(
            StockChangeResult result
    ) {
        super();
        this.result = result;
    }

    public StockChangeResult getResult() {
        return result;
    }

    public Long getProductId() {
        return result.productId();
    }

    @Override
    public String toString() {
        return "ProductStockUpdatedEvent{" +
                "productId=" + result.productId() +
                ", sellerId=" + result.sellerId() +
                ", beforeStock=" + result.beforeStock() +
                ", afterStock=" + result.afterStock() +
                ", delta=" + result.delta() +
                ", changeType=" + result.changeType() +
                '}';
    }
}
