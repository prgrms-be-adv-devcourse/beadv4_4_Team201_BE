package app.giftify.orderDemo.adapter.outbound.persistence.jpa;

import app.giftify.orderDemo.domain.OrderItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaOrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT oi FROM OrderItem oi " +
            "WHERE oi.order.id = :orderId " +
            "AND oi.status IN (:#{T(app.giftify.orderDemo.domain.OrderItemStatus).CREATED}, :#{T(app.giftify.orderDemo.domain.OrderItemStatus).PAID})")
    List<OrderItem> findCancelableItemsByOrderId(@Param("orderId") Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT oi FROM OrderItem oi " +
            "WHERE oi.order.id = :orderId " +
            "AND oi.status = :#{T(app.giftify.orderDemo.domain.OrderItemStatus).CANCEL_PENDING}")
    List<OrderItem> findPendingCancelItemsByOrderId(@Param("orderId") Long orderId);
}
