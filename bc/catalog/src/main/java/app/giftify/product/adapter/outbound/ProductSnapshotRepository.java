package app.giftify.product.adapter.outbound;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.product.domain.ProductSnapshot;

public interface ProductSnapshotRepository extends JpaRepository<ProductSnapshot, Long> {
}
