package app.giftify.orderDemo.adapter.outbound.persistence;

import app.giftify.orderDemo.adapter.outbound.persistence.jpa.JpaOrderItemRepository;
import app.giftify.orderDemo.application.outbound.port.OrderItemRepository;
import app.giftify.orderDemo.domain.OrderItem;
import app.giftify.orderDemo.domain.OrderItemStatus;
import app.giftify.orderDemo.domain.errorCode.OrderErrorCode;
import app.giftify.shared.api.exception.PolicyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    public List<OrderItem> getCancelableItemsByOrderId(Long orderId) {
        return jpaOrderItemRepository.findCancelableItemsByOrderId(orderId);
    }

    @Override
    public List<OrderItem> getPendingCancelItemsByOrderId(Long orderId) {
        return jpaOrderItemRepository.findPendingCancelItemsByOrderId(orderId);
    }

    @Override
    public List<OrderItem> getAllByOrderIdAndIdIn(Long orderId, List<Long> itemIds) {
        return jpaOrderItemRepository.findAllByOrderIdAndIdIn(orderId, itemIds);
    }

    @Override
    public List<OrderItemStatus> getStatusesByOrderId(Long orderId) {
        return jpaOrderItemRepository.findStatusByOrderId(orderId);
    }

    @Override
    public void flush() {
        jpaOrderItemRepository.flush();
    }
}
