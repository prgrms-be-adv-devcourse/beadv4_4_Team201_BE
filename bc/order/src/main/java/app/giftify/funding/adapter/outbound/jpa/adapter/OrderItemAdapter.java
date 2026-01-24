package app.giftify.funding.adapter.outbound.jpa.adapter;

import app.giftify.funding.adapter.outbound.jpa.entity.OrderItemEntity;
import app.giftify.funding.adapter.outbound.jpa.mapper.OrderItemMapper;
import app.giftify.funding.adapter.outbound.jpa.repository.OrderItemRepository;
import app.giftify.funding.application.outbound.OrderItemRepositoryPort;
import app.giftify.funding.domain.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
}
