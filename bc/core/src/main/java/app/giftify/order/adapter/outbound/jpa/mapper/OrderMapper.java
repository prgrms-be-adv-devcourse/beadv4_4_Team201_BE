package app.giftify.order.adapter.outbound.jpa.mapper;

import java.util.List;

import app.giftify.order.adapter.outbound.jpa.entity.OrderEntity;
import app.giftify.order.domain.Order;
import app.giftify.order.domain.vo.Money;

public class OrderMapper {

    public static OrderEntity toEntity(Order order) {
        return OrderEntity.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .buyerId(order.getBuyerId())
                .totalAmount(order.getTotalAmount().amount())
                .paymentMethod(order.getPaymentMethod())
                .paymentKey(order.getPaymentKey())
                .status(order.getStatus())
                .confirmedAt(order.getConfirmedAt())
                .cancelledAt(order.getCancelledAt())
                .build();
    }

    public static Order toDomain(OrderEntity entity) {
        return Order.builder()
                .id(entity.getId())
                .orderNumber(entity.getOrderNumber())
                .buyerId(entity.getBuyerId())
                .totalAmount(Money.of(entity.getTotalAmount()))
                .paymentMethod(entity.getPaymentMethod())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .paymentKey(entity.getPaymentKey())
                .confirmedAt(entity.getConfirmedAt())
                .cancelledAt(entity.getCancelledAt())
                .build();
    }

    public static List<OrderEntity> toEntityList(List<Order> orders) {
        return orders.stream()
                .map(OrderMapper::toEntity)
                .toList();
    }

    public static List<Order> toDomainList(List<OrderEntity> entities) {
        return entities.stream()
                .map(OrderMapper::toDomain)
                .toList();
    }
}
