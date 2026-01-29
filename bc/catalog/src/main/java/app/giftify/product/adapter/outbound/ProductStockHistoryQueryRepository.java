package app.giftify.product.adapter.outbound;

import org.springframework.data.domain.Page;

import app.giftify.product.domain.ProductStockHistory;
import app.giftify.product.adapter.inbound.StockHistorySearchDto;

public interface ProductStockHistoryQueryRepository {
	Page<ProductStockHistory> searchStockHistories(Long sellerId, StockHistorySearchDto searchDto);
}
