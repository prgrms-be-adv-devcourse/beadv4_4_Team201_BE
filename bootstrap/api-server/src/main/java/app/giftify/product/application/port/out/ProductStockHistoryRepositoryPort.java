package app.giftify.product.application.port.out;

import app.giftify.product.application.port.in.StockHistorySearchCommand;
import app.giftify.product.domain.ProductStockHistory;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ProductStockHistoryRepositoryPort {
    ProductStockHistory save(ProductStockHistory productStockHistory);

    Optional<ProductStockHistory> findById(Long productStockHistoryId);

    // QueryDsl
    Page<ProductStockHistory> searchStockHistories(Long sellerId, StockHistorySearchCommand searchCommand);

    // Hard Delete하는 상품과 관련된 재고 이력 삭제
    int deleteByProductIds(List<Long> productIds);
}
