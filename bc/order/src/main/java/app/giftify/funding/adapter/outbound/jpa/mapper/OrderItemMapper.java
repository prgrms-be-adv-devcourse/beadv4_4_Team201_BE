package app.giftify.funding.adapter.outbound.jpa.mapper;

import app.giftify.funding.adapter.outbound.jpa.entity.OrderItemEntity;
import app.giftify.funding.domain.OrderItem;
import app.giftify.funding.domain.vo.Money;
import app.giftify.funding.domain.vo.Quantity;

import java.util.List;

public class OrderItemMapper {
    public static OrderItemEntity toEntity(OrderItem orderItem) {
        return OrderItemEntity.builder()
                .id(orderItem.getId())
                .orderId(orderItem.getOrderId())
                .targetSnapshotId(orderItem.getTargetSnapshotId())
                .targetType(orderItem.getTargetType())
                .sellerId(orderItem.getSellerId())
                .receiverId(orderItem.getReceiverId())
                .price(orderItem.getPrice().amount())
                .quantity(orderItem.getQuantity().getValue())
                .confirmedAt(orderItem.getConfirmedAt())
                .cancelledAt(orderItem.getCancelledAt())
                .build();
    }

    public static OrderItem toDomain(OrderItemEntity entity) {
        return OrderItem.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .targetSnapshotId(entity.getTargetSnapshotId())
                .targetType(entity.getTargetType())
                .sellerId(entity.getSellerId())
                .receiverId(entity.getReceiverId())
                .price(Money.of(entity.getPrice()))
                .quantity(new Quantity(entity.getQuantity()))
                .createdAt(entity.getCreatedAt())
                .confirmedAt(entity.getConfirmedAt())
                .cancelledAt(entity.getCancelledAt())
                .build();
    }

    public static List<OrderItemEntity> toEntities(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(OrderItemMapper::toEntity)
                .toList();
    }

    public static List<OrderItem> toDomains(List<OrderItemEntity> orderItemEntities){
        return orderItemEntities.stream()
                .map(OrderItemMapper::toDomain)
                .toList();
    }
}
