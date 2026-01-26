package app.giftify.funding.adapter.outbound.jpa.adapter;

import app.giftify.funding.adapter.outbound.jpa.entity.OrderEntity;
import app.giftify.funding.adapter.outbound.jpa.mapper.OrderMapper;
import app.giftify.funding.adapter.outbound.jpa.repository.OrderRepository;
import app.giftify.funding.application.outbound.OrderRepositoryPort;
import app.giftify.funding.domain.Order;
import app.giftify.funding.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderAdapter implements OrderRepositoryPort {

    private final OrderRepository orderRepository;

    @Override
    public Order save(Order order) {
        OrderEntity entity = OrderMapper.toEntity(order);
        OrderEntity savedEntity = orderRepository.save(entity);
        return OrderMapper.toDomain(savedEntity);
    }

    @Override
    public List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime threshold) {
        List<OrderEntity> entities = orderRepository.findByStatusAndCreatedAtBefore(status, threshold);
        return OrderMapper.toDomainList(entities);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id).map(OrderMapper::toDomain);
    }

    @Override
    public void delete(Order order) {
        orderRepository.deleteById(order.getId());
    }

    @Override
    public Optional<Order> findByIdAndBuyerId(Long orderId, Long memberId) {
        return orderRepository.findByIdAndBuyerId(orderId, memberId)
                .map(OrderMapper::toDomain);
    }
}
