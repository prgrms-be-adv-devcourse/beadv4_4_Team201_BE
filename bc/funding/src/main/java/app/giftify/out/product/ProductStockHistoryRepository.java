package app.giftify.out.product;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.domain.product.ProductStockHistory;

public interface ProductStockHistoryRepository
	extends JpaRepository<ProductStockHistory, Long>, ProductStockHistoryQueryRepository {
}
