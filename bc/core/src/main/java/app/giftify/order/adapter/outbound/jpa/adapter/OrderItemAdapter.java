package app.giftify.order.adapter.outbound.jpa.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import app.giftify.order.adapter.outbound.jpa.entity.OrderItemEntity;
import app.giftify.order.adapter.outbound.jpa.mapper.OrderItemMapper;
import app.giftify.order.adapter.outbound.jpa.repository.OrderItemRepository;
import app.giftify.order.application.outbound.OrderItemRepositoryPort;
import app.giftify.order.domain.OrderItem;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderItemAdapter implements OrderItemRepositoryPort {

    private final OrderItemRepository orderItemRepository;

    @Override
    public List<OrderItem> saveAll(List<OrderItem> orderItems) {
        List<OrderItemEntity> entities = OrderItemMapper.toEntities(orderItems);
        List<OrderItemEntity> savedEntities = orderItemRepository.saveAll(entities);
        return OrderItemMapper.toDomains(savedEntities);
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        List<OrderItemEntity> entities = orderItemRepository.findByOrderId(orderId);
        return OrderItemMapper.toDomains(entities);
    }

    @Override
    public void deleteAll(List<OrderItem> orderItems) {
        List<OrderItemEntity> entities = OrderItemMapper.toEntities(orderItems);
        orderItemRepository.deleteAll(entities);
    }
}
