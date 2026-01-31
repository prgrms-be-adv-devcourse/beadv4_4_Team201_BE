package app.giftify.orderDemo.adapter.outbound.persistence;

import app.giftify.orderDemo.adapter.outbound.persistence.jpa.JpaOrderItemRepository;
import app.giftify.orderDemo.application.outbound.port.OrderItemRepository;
import app.giftify.orderDemo.domain.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderItemAdapter implements OrderItemRepository {

    private final JpaOrderItemRepository jpaOrderItemRepository;

    @Override
    public OrderItem save(OrderItem orderItem) {
        return jpaOrderItemRepository.save(orderItem);
    }
}
