package app.giftify.order.adapter.outbound.persistence;

import app.giftify.order.adapter.outbound.persistence.jpa.JpaOrderRepository;
import app.giftify.order.application.outbound.port.OrderRepository;
import app.giftify.order.domain.Order;
import app.giftify.order.domain.OrderStatus;
import app.giftify.order.domain.errorCode.OrderErrorCode;
import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.PolicyException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
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

    @Override
    public Order getById(Long orderId) {
        return jpaOrderRepository.findById(orderId)
                .orElseThrow(() -> new PolicyException(OrderErrorCode.ORDER_NOT_FOUND, "orderId = " + orderId));
    }

    @Override
    public Order getByOrderNumber(String orderNumber) {
        return jpaOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new PolicyException(OrderErrorCode.ORDER_NOT_FOUND, "orderNumber = " + orderNumber));
    }

    @Override
    public Order getByIdWithItemsAndLock(Long id) {
        return jpaOrderRepository.findByIdWithItemsAndLock(id)
                .orElseThrow(() -> new PolicyException(OrderErrorCode.ORDER_NOT_FOUND, "orderId = " + id));
    }

    @Override
    public List<Order> getAllByIdInWithItems(List<Long> ids) {
        return jpaOrderRepository.findAllByIdInWithItems(ids);
    }

    @Override
    public List<Long> getIdsByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime threshold) {
        return jpaOrderRepository.findIdsByStatusAndCreatedAtBefore(status, threshold);
    }

    @Override
    public Order getByIdWithItems(Long id) {
        return jpaOrderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new DomainException(OrderErrorCode.ORDER_NOT_FOUND, "orderId = " + id));
    }
}
