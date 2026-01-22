package app.giftify.order.adapter.out.jpa.repository;

import app.giftify.order.adapter.out.jpa.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrderEntity o where o.id = :id")
    Optional<OrderEntity> findByIdWithLock(@Param("id") Long id);

    @Query("select o from OrderEntity o where o.status = app.giftify.order.domain.domain.OrderStatus.PAYMENT_PENDING and o.createdAt < :time")
    List<OrderEntity> findAllPaymentPendingOlderThan(@Param("time") LocalDateTime time);

    @Query("select distinct o from OrderEntity o join o.orderItems i where i.fundingId = :fundingId")
    List<OrderEntity> findAllByFundingId(@Param("fundingId") Long fundingId);
}
