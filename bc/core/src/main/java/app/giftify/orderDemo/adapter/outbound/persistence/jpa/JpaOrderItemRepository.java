package app.giftify.orderDemo.adapter.outbound.persistence.jpa;

import app.giftify.orderDemo.domain.OrderItem;
import app.giftify.shared.domain.type.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaOrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi FROM OrderItem oi " +
            "JOIN FETCH oi.order " +
            "WHERE oi.targetId = :targetId " +
            "AND oi.targetType = :targetType")
    List<OrderItem> findOrderItemsWithOrderAndFindingId(
            @Param("targetId") Long targetId,
            @Param("targetType") TargetType targetType
    );
}
