package app.giftify.product.adapter.outbound.jpa.repository;

import app.giftify.product.adapter.outbound.jpa.entity.ProductSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSnapshotRepository extends JpaRepository<ProductSnapshot, Long> {
}
