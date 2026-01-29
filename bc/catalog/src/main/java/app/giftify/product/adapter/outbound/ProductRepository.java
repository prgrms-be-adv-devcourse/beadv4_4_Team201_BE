package app.giftify.product.adapter.outbound;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.product.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductQueryRepository {
	Optional<Product> findByIdAndSellerId(Long id, Long sellerId);
}
