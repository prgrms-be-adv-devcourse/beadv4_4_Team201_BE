package app.giftify.orderDemo.adapter.outbound.persistence.jpa;

import app.giftify.orderDemo.domain.OrderItem;
import app.giftify.orderDemo.domain.OrderItemStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaOrderItemRepository extends JpaRepository<OrderItem, Long> {

    // todo:
    //  1. 파라미터에 상태 포함
    //  2. 메서드명 변경,
    //  3. findCancelableItemsByOrderId & findPendingCancelItemsByOrderId 메서드 통합 -> findAllForCancel
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT oi FROM OrderItem oi " +
            "WHERE oi.order.id = :orderId " +
            "AND oi.status IN (:#{T(app.giftify.orderDemo.domain.OrderItemStatus).CREATED}, :#{T(app.giftify.orderDemo.domain.OrderItemStatus).PAID})")
    List<OrderItem> findCancelableItemsByOrderId(@Param("orderId") Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT oi FROM OrderItem oi " +
            "WHERE oi.order.id = :orderId " +
            "AND oi.status = :#{T(app.giftify.orderDemo.domain.OrderItemStatus).CANCELING}")
    List<OrderItem> findPendingCancelItemsByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT oi FROM OrderItem oi " +
            "WHERE oi.order.id = :orderId " +
            "AND oi.id IN :itemIds ")
    List<OrderItem> findAllByOrderIdAndIdIn(
            @Param("orderId") Long orderId,
            @Param("itemIds") List<Long> itemIds
    );

    List<OrderItemStatus> findStatusByOrderId(Long orderId);
}
