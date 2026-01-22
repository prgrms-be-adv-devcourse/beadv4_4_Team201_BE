package app.giftify.order.adapter.out.jpa.mapper;

import app.giftify.order.adapter.out.jpa.entity.OrderItemEntity;
import app.giftify.order.adapter.out.jpa.entity.OrderEntity;
import app.giftify.order.domain.domain.Order;
import app.giftify.order.domain.domain.OrderItem;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.Quantity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderEntity toOrderEntity(Order order) {
        OrderEntity orderEntity = OrderEntity.builder()
                .orderNumber(order.getOrderNumber())
                .buyerId(order.getBuyerId())
                .totalAmount(order.getTotalAmount().amount())
                .status(order.getStatus())
                .paymentKey(order.getPaymentKey())
                .confirmedAt(order.getConfirmedAt())
                .cancelledAt(order.getCancelledAt())
                .orderItems(order.getOrderItems().stream()
                        .map(this::toItemEntity)
                        .collect(Collectors.toList()))
                .build();
        
        return orderEntity;
    }

    public Order toOrderDomain(OrderEntity entity) {
        return Order.builder()
                .id(entity.getId())
                .orderNumber(entity.getOrderNumber())
                .buyerId(entity.getBuyerId())
                .totalAmount(Money.of(entity.getTotalAmount()))
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .paymentKey(entity.getPaymentKey())
                .confirmedAt(entity.getConfirmedAt())
                .cancelledAt(entity.getCancelledAt())
                .orderItems(entity.getOrderItems().stream()
                        .map(this::toItemDomain)
                        .collect(Collectors.toList()))
                .build();
    }

    private OrderItemEntity toItemEntity(OrderItem item) {
        return OrderItemEntity.builder()
                .orderId(item.getOrderId())
                .fundingId(item.getFundingId())
                .productId(item.getProductId())
                .sellerId(item.getSellerId())
                .receiverId(item.getReceiverId())
                .price(item.getPrice().amount())
                .quantity(item.getQuantity().getValue())
                .status(item.getStatus())
                .confirmedAt(item.getConfirmedAt())
                .cancelledAt(item.getCancelledAt())
                .build();
    }

    private OrderItem toItemDomain(OrderItemEntity entity) {
        return OrderItem.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .fundingId(entity.getFundingId())
                .productId(entity.getProductId())
                .sellerId(entity.getSellerId())
                .receiverId(entity.getReceiverId())
                .price(Money.of(entity.getPrice()))
                .quantity(Quantity.of(entity.getQuantity()))
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .confirmedAt(entity.getConfirmedAt())
                .canceledAt(entity.getCancelledAt())
                .build();
    }
}
