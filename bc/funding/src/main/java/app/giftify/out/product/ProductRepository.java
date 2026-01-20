package app.giftify.out.product;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.domain.product.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductQueryRepository {
	Optional<Product> findByIdAndSellerId(Long id, Long sellerId);
}
