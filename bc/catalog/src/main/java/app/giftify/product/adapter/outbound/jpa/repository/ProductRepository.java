package app.giftify.product.adapter.outbound.jpa.repository;

import app.giftify.product.adapter.outbound.jpa.entity.ProductJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductJpa, Long>, ProductQueryRepository {
    Optional<ProductJpa> findByIdAndSellerId(Long id, Long sellerId);
}
