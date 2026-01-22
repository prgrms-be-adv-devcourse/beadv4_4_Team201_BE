package app.giftify.order.adapter.out.jpa.mapper;

import app.giftify.order.adapter.out.jpa.entity.OrderEntity;
import app.giftify.order.adapter.out.jpa.entity.OrderItemEntity;
import app.giftify.order.domain.domain.Order;
import app.giftify.order.domain.domain.OrderItem;
import app.giftify.order.domain.domain.OrderStatus;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderMapperTest {

    private final OrderMapper orderMapper = new OrderMapper();

    @Test
    @DisplayName("도메인 객체를 엔티티로 정상적으로 변환한다")
    void toOrderEntity_Success() {
        // given
        OrderItem item = OrderItem.builder()
                .fundingId(10L)
                .productId(100L)
                .sellerId(1000L)
                .receiverId(1L)
                .price(Money.of(5000))
                .quantity(Quantity.of(2))
                .status(OrderStatus.PAYMENT_PENDING)
                .build();

        Order order = Order.builder()
                .orderNumber("ORD-123456")
                .buyerId(1L)
                .totalAmount(Money.of(10000))
                .status(OrderStatus.PAYMENT_PENDING)
                .orderItems(List.of(item))
                .build();

        // when
        OrderEntity entity = orderMapper.toOrderEntity(order);

        // then
        assertEquals(order.getOrderNumber(), entity.getOrderNumber());
        assertEquals(order.getBuyerId(), entity.getBuyerId());
        assertEquals(new BigDecimal(10000), entity.getTotalAmount());
        assertEquals(order.getStatus(), entity.getStatus());
        assertEquals(1, entity.getOrderItems().size());
        assertEquals(new BigDecimal(5000), entity.getOrderItems().get(0).getPrice());
        assertEquals(2, entity.getOrderItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("엔티티를 도메인 객체로 정상적으로 변환한다")
    void toOrderDomain_Success() {
        // given
        OrderItemEntity itemEntity = OrderItemEntity.builder()
                .fundingId(10L)
                .productId(100L)
                .sellerId(1000L)
                .receiverId(1L)
                .price(new BigDecimal(5000))
                .quantity(2)
                .status(OrderStatus.PAYMENT_PENDING)
                .build();

        OrderEntity orderEntity = OrderEntity.builder()
                .orderNumber("ORD-123456")
                .buyerId(1L)
                .totalAmount(new BigDecimal(10000))
                .status(OrderStatus.PAYMENT_PENDING)
                .orderItems(List.of(itemEntity))
                .build();

        // when
        Order order = orderMapper.toOrderDomain(orderEntity);

        // then
        assertEquals(orderEntity.getOrderNumber(), order.getOrderNumber());
        assertEquals(orderEntity.getBuyerId(), order.getBuyerId());
        assertEquals(Money.of(10000), order.getTotalAmount());
        assertEquals(orderEntity.getStatus(), order.getStatus());
        assertEquals(1, order.getOrderItems().size());
        assertEquals(Money.of(5000), order.getOrderItems().get(0).getPrice());
        assertEquals(2, order.getOrderItems().get(0).getQuantity().getValue());
    }
}
