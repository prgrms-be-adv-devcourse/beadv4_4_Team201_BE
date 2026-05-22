package app.giftify.order.adapter.outbound.persistence.jpa;

import app.giftify.order.domain.Order;
import app.giftify.order.domain.OrderStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JpaOrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);

    Optional<Order> findByOrderNumber(String orderNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o join fetch o.items where o.id = :id")
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    })
    Optional<Order> findByIdWithItemsAndLock(@Param("id") Long id);

    @Query("select distinct o from Order o join fetch o.items where o.id in :ids")
    List<Order> findAllByIdInWithItems(@Param("ids") List<Long> ids);

    @Query("SELECT o.id FROM Order o WHERE o.status = :status AND o.createdAt <= :threshold")
    List<Long> findIdsByStatusAndCreatedAtBefore(
            @Param("status") OrderStatus status,
            @Param("threshold") LocalDateTime threshold
    );

    @Query("select o from Order o join fetch o.items where o.id = :id")
    Optional<Order> findByIdWithItems(Long id);
}
