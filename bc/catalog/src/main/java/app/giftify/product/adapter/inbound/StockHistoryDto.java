package app.giftify.product.adapter.inbound;

import java.time.LocalDateTime;

import app.giftify.product.domain.ProductStockHistory;
import app.giftify.product.domain.StockChangeType;

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
