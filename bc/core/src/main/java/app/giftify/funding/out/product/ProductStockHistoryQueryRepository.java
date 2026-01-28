package app.giftify.funding.out.product;

import org.springframework.data.domain.Page;

import app.giftify.funding.domain.product.ProductStockHistory;
import app.giftify.funding.in.product.StockHistorySearchDto;

public interface ProductStockHistoryQueryRepository {
	Page<ProductStockHistory> searchStockHistories(Long sellerId, StockHistorySearchDto searchDto);
}
