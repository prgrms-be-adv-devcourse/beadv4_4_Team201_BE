package app.giftify.product.adapter.outbound.jpa.repository;

import app.giftify.product.adapter.outbound.jpa.entity.ProductJpa;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

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

}