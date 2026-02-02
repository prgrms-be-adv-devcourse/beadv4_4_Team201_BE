package app.giftify.product.adapter.outbound.jpa.repository;

import app.giftify.product.adapter.outbound.jpa.entity.ProductStockHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductStockHistoryRepository
        extends JpaRepository<ProductStockHistory, Long>, ProductStockHistoryQueryRepository {
}
