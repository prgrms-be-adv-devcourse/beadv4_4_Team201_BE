package app.giftify.product.application.port.out;

import app.giftify.product.application.port.in.StockHistorySearchCommand;
import app.giftify.product.domain.ProductStockHistory;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface ProductStockHistoryRepositoryPort {
    ProductStockHistory save(ProductStockHistory productStockHistory);

    Optional<ProductStockHistory> findById(Long productStockHistoryId);

    // QueryDsl
    Page<ProductStockHistory> searchStockHistories(Long sellerId, StockHistorySearchCommand searchCommand);

}
