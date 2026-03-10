package app.giftify.product.adapter.outbound.jpa.repository;

import app.giftify.product.adapter.outbound.jpa.entity.ProductJpa;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductJpa, Long>, ProductQueryRepository {
    Optional<ProductJpa> findByIdAndSellerId(Long id, Long sellerId);

    /**
     * 재고 동시성 제어 : 비관적 락 배타 잠금
     * 락 범위를 재고 변경 시에만 적용할 수 있도록한 전용 데이터 조회 메서드
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")) // 무한 대기 방지 3초 타임아웃
    @Query("SELECT p FROM ProductJpa p WHERE p.id = :id")
    Optional<ProductJpa> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT p.id FROM ProductJpa p WHERE p.deletedAt IS NOT NULL AND p.deletedAt < :cutoff")
    List<Long> findExpiredDeletedProductIds(@Param("cutoff") LocalDateTime cutoff);

    @Modifying(clearAutomatically = true) // 쿼리 실행 후 자동으로 em.clear()를 호출해서 영속성 컨텍스트를 초기화
    @Query("DELETE FROM ProductJpa p WHERE p.deletedAt IS NOT NULL AND p.deletedAt < :cutoff")
    int deleteExpiredProducts(@Param("cutoff") LocalDateTime cutoff);
}