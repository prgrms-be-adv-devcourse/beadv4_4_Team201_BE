package app.giftify.product.adapter.outbound.jpa;

import app.giftify.product.adapter.outbound.jpa.entity.ProductStockHistoryJpa;
import app.giftify.product.domain.ProductStockHistory;
import org.springframework.stereotype.Component;

@Component
public class ProductStockHistoryMapper { // ProductStockHistory는 항상 INSERT만, UPDATE XX

    public ProductStockHistory toDomain(ProductStockHistoryJpa entity) {
        if (entity == null)
            return null;
        return ProductStockHistory.builder()
                .id(entity.getId())
                .sellerId(entity.getSellerId())
                .productId(entity.getProductId())
                .delta(entity.getDelta())
                .beforeStock(entity.getBeforeStock())
                .afterStock(entity.getAfterStock())
                .changeType(entity.getChangeType())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public ProductStockHistoryJpa toEntity(ProductStockHistory domain) {
        if (domain == null)
            return null;
        return ProductStockHistoryJpa.builder()
                .sellerId(domain.getSellerId())
                .productId(domain.getProductId())
                .delta(domain.getDelta())
                .beforeStock(domain.getBeforeStock())
                .afterStock(domain.getAfterStock())
                .changeType(domain.getChangeType())
                .build();
    }
}
