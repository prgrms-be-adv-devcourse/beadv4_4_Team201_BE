package app.giftify.funding.out.product;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.funding.domain.product.ProductSnapshot;

public interface ProductSnapshotRepository extends JpaRepository<ProductSnapshot, Long> {
}
