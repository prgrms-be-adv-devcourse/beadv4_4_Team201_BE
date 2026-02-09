package app.giftify.product.adapter.inbound.web.responseDto;

import app.giftify.product.domain.ProductStockHistory;
import app.giftify.product.domain.StockChangeType;

import java.time.LocalDateTime;

public record StockHistoryDto(
        Long id,
        Long sellerId,
        Long productId,
        int delta,
        int beforeStock,
        int afterStock,
        StockChangeType changeType,
        LocalDateTime createdAt
) {
    public static StockHistoryDto from(ProductStockHistory history) {
        if (history == null)
            return null;

        return new StockHistoryDto(
                history.getId(),
                history.getSellerId(),
                history.getProductId(),
                history.getDelta(),
                history.getBeforeStock(),
                history.getAfterStock(),
                history.getChangeType(),
                history.getCreatedAt()
        );
    }
}
