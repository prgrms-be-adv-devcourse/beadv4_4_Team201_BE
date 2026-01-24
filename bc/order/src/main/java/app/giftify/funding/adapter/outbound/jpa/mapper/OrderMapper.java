package app.giftify.funding.adapter.outbound.jpa.mapper;

import app.giftify.funding.adapter.outbound.jpa.entity.OrderEntity;
import app.giftify.funding.domain.Order;
import app.giftify.shared.domain.vo.Money;

import java.util.List;

public class OrderMapper {

    public static OrderEntity toEntity(Order order) {
        return OrderEntity.builder()
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
