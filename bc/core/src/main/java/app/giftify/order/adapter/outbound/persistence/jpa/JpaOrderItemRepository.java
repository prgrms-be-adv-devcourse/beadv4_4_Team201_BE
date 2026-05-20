package app.giftify.order.adapter.outbound.persistence.jpa;

import app.giftify.order.domain.OrderItem;
import app.giftify.shared.domain.type.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface JpaOrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi FROM OrderItem oi " +
            "JOIN FETCH oi.order " +
            "WHERE oi.targetId = :targetId " +
            "AND oi.targetType = :targetType")
    List<OrderItem> findOrderItemsWithOrderAndFindingId(
            @Param("targetId") Long targetId,
            @Param("targetType") TargetType targetType
    );

    @Query("SELECT oi.order.id as orderId, oi.id as itemId FROM OrderItem oi " +
            "WHERE oi.targetId = :targetId AND oi.targetType = :targetType")
    List<OrderIdMapping> findRawMappings(@Param("targetId") Long fundingId,
                                         @Param("targetType") TargetType targetType);

    default Map<Long, List<Long>> findItemIdMapByFundingId(Long fundingId, TargetType targetType) {
        return findRawMappings(fundingId, targetType).stream()
                .collect(Collectors.groupingBy(
                        OrderIdMapping::getOrderId,
                        Collectors.mapping(OrderIdMapping::getItemId, Collectors.toList())
                ));
    }

    interface OrderIdMapping {
        Long getOrderId();
        Long getItemId();
    }

    @Modifying(clearAutomatically = true)
    @Query("UPDATE OrderItem oi SET oi.status = 'CONFIRMED' " +
            "WHERE oi.id IN :ids AND oi.status = 'PAID'")
    int confirmOrderItems(@Param("ids") List<Long> ids);
}
