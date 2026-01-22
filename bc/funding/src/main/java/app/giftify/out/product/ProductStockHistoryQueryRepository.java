package app.giftify.out.product;

import org.springframework.data.domain.Page;

import app.giftify.domain.product.ProductStockHistory;
import app.giftify.in.product.StockHistorySearchDto;

public interface ProductStockHistoryQueryRepository {
	Page<ProductStockHistory> searchStockHistories(Long sellerId, StockHistorySearchDto searchDto);
}