package app.giftify.product.adapter.outbound;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.product.domain.ProductStockHistory;

public interface ProductStockHistoryRepository
	extends JpaRepository<ProductStockHistory, Long>, ProductStockHistoryQueryRepository {
}
