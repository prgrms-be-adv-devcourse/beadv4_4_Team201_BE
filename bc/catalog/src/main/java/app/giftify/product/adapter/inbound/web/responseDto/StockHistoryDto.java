package app.giftify.product.adapter.inbound.web.responseDto;

import app.giftify.product.adapter.outbound.jpa.entity.ProductStockHistory;
import app.giftify.product.domain.StockChangeType;

import java.time.LocalDateTime;

public record StockHistoryDto(
        Long id,
        Long productId,
        StockChangeType changeType,
        int delta,
        int beforeStock,
        int afterStock,
        LocalDateTime createdAt
) {
    public static StockHistoryDto from(ProductStockHistory history) {
        return new StockHistoryDto(
                history.getId(),
                history.getProductId(),
                history.getChangeType(),
                history.getDelta(),
                history.getBeforeStock(),
                history.getAfterStock(),
                history.getCreatedAt()
        );
    }
}
