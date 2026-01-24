package app.giftify.funding.adapter.outbound.jpa.adapter;

import app.giftify.funding.adapter.outbound.jpa.entity.OrderEntity;
import app.giftify.funding.adapter.outbound.jpa.mapper.OrderMapper;
import app.giftify.funding.adapter.outbound.jpa.repository.OrderRepository;
import app.giftify.funding.application.outbound.OrderRepositoryPort;
import app.giftify.funding.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}
