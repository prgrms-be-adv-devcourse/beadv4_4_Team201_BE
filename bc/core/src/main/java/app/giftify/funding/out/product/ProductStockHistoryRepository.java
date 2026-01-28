package app.giftify.funding.out.product;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.funding.domain.product.ProductStockHistory;

public interface ProductStockHistoryRepository
	extends JpaRepository<ProductStockHistory, Long>, ProductStockHistoryQueryRepository {
}
