package app.giftify.product.domain;

import app.giftify.support.common.base.BaseDomainModel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProductStockHistory extends BaseDomainModel {
    private final Long sellerId;
    private final Long productId;
    private final StockChangeType changeType;
    private final int delta; // 재고 변화량 (-5, +10, -1 등)
    private final int beforeStock; // 변경 전 재고량
    private final int afterStock; // 변경 후 재고량
    private final LocalDateTime createdAt;

    @Builder
    public ProductStockHistory(
            Long id, Long sellerId, Long productId,
            int delta, int beforeStock, int afterStock,
            StockChangeType changeType, LocalDateTime createdAt
    ) {
        super(id);
        this.sellerId = sellerId;
        this.productId = productId;
        this.delta = delta;
        this.beforeStock = beforeStock;
        this.afterStock = afterStock;
        this.changeType = changeType;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }
}
