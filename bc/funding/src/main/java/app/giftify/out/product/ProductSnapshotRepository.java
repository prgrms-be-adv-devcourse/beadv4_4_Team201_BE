package app.giftify.out.product;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.domain.product.ProductSnapshot;

public interface ProductSnapshotRepository extends JpaRepository<ProductSnapshot, Long> {
}