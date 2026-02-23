package app.giftify.order.adapter.outbound.persistence;

import app.giftify.order.adapter.outbound.persistence.jpa.JpaOrderItemRepository;
import app.giftify.order.application.outbound.port.OrderItemRepository;
import app.giftify.order.domain.OrderItem;
import app.giftify.order.domain.errorCode.OrderErrorCode;
import app.giftify.shared.api.exception.PolicyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}
