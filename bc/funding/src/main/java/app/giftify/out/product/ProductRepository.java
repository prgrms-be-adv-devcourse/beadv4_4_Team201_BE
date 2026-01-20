package app.giftify.out.product;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.domain.product.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductQueryRepository {
}
