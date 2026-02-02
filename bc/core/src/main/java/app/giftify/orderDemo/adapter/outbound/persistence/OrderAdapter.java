package app.giftify.orderDemo.adapter.outbound.persistence;

import app.giftify.orderDemo.adapter.outbound.persistence.jpa.JpaOrderRepository;
import app.giftify.orderDemo.application.outbound.port.OrderRepository;
import app.giftify.orderDemo.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class OrderAdapter implements OrderRepository {

    private final JpaOrderRepository jpaOrderRepository;

    @Override
    public Order save(Order order) {
        return jpaOrderRepository.save(order);
    }

    @Override
    public Page<Order> getByBuyerId(Long buyerId, Pageable pageable) {
        return jpaOrderRepository.findByBuyerId(buyerId, pageable);
    }
}
