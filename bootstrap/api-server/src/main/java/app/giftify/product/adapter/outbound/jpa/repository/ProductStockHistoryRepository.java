package app.giftify.product.adapter.outbound.jpa.repository;

import app.giftify.product.adapter.outbound.jpa.entity.ProductStockHistoryJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductStockHistoryRepository
        extends JpaRepository<ProductStockHistoryJpa, Long>, ProductStockHistoryQueryRepository {

    // JPQL 벌크 DELETE
    @Modifying(clearAutomatically = true) // 쿼리 실행 후 자동으로 em.clear()를 호출해서 영속성 컨텍스트를 초기화
    @Query("DELETE FROM ProductStockHistoryJpa h WHERE h.productId IN :productIds")
    int deleteByProductIdIn(@Param("productIds") List<Long> productIds);
}
