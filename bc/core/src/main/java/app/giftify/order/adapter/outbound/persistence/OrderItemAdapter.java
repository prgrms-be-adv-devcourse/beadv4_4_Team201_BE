package app.giftify.order.adapter.outbound.persistence;

import app.giftify.order.adapter.outbound.persistence.jpa.JpaOrderItemRepository;
import app.giftify.order.application.outbound.port.OrderItemRepository;
import app.giftify.order.domain.OrderItem;
import app.giftify.order.domain.errorCode.OrderErrorCode;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.shared.domain.type.TargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository("orderItemV2Adapter")
@RequiredArgsConstructor
public class OrderItemAdapter implements OrderItemRepository {

    private final JpaOrderItemRepository jpaOrderItemRepository;

    @Override
    public OrderItem save(OrderItem orderItem) {
        return jpaOrderItemRepository.save(orderItem);
    }

    @Override
    public OrderItem getOrderItemById(Long id) {
        return jpaOrderItemRepository.findById(id)
                .orElseThrow(() -> new PolicyException(OrderErrorCode.ORDER_ITEM_NOT_FOUND));
    }

    @Override
    public List<OrderItem> getOrderItemsWithOrderAndFindingId(Long findingId) {
        return jpaOrderItemRepository.findOrderItemsWithOrderAndFindingId(findingId, TargetType.FUNDING);
    }

    @Override
    public Map<Long, List<Long>> getItemIdMapByFundingId(Long fundingId) {
        return jpaOrderItemRepository.findItemIdMapByFundingId(fundingId, TargetType.FUNDING);
    }

    @Override
    public int confirmOrderItems(List<Long> ids) {
        return jpaOrderItemRepository.confirmOrderItems(ids);
    }


}
