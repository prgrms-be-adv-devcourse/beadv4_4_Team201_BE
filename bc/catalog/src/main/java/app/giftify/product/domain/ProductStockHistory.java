package app.giftify.product.domain;

import app.giftify.shared.domain.base.BaseDomainModel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProductStockHistory extends BaseDomainModel {
    private final Long sellerId;
    private final Long productId;
    private final StockChangeType changeType;
    private final int delta;
    private final int beforeStock;
    private final int afterStock;
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
        this.createdAt = createdAt;
    }

    // 주문 차감 이력 생성 case
    public static ProductStockHistory orderDeduct(
            Long sellerId, Long productId, int quantity, int beforeStock, int afterStock
    ) {
        return ProductStockHistory.builder()
                .sellerId(sellerId)
                .productId(productId)
                .delta(-quantity)
                .beforeStock(beforeStock)
                .afterStock(afterStock)
                .changeType(StockChangeType.ORDER_COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // 재고 복원 이력 생성 case
    public static ProductStockHistory orderRestore(
            Long sellerId, Long productId, int quantity, int beforeStock, int afterStock
    ) {
        return ProductStockHistory.builder()
                .sellerId(sellerId)
                .productId(productId)
                .delta(quantity)
                .beforeStock(beforeStock)
                .afterStock(afterStock)
                .changeType(StockChangeType.ORDER_REFUNDED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // 판매자 수동 재고 변경 이력 생성 case
    public static ProductStockHistory manualAdjust(
            Long sellerId, Long productId, int delta, int beforeStock, int afterStock
    ) {
        return ProductStockHistory.builder()
                .sellerId(sellerId)
                .productId(productId)
                .delta(delta)
                .beforeStock(beforeStock)
                .afterStock(afterStock)
                .changeType(StockChangeType.MANUAL_SELLER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // todo 관리자 수동 재고 번경 이력 생성 case
}
