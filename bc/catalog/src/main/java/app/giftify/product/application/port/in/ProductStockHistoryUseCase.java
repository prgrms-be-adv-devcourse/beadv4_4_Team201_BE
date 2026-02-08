package app.giftify.product.application.port.in;

import app.giftify.product.domain.ProductStockHistory;
import org.springframework.data.domain.Page;

public interface ProductStockHistoryUseCase {
    Page<ProductStockHistory> searchStockHistories(Long sellerId, StockHistorySearchCommand searchCommand);
}
