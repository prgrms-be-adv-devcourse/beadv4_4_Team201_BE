package app.giftify.funding.adapter.outbound.jpa.repository;

import app.giftify.OrderTestApplication;
import app.giftify.funding.adapter.outbound.jpa.entity.OrderEntity;
import app.giftify.funding.adapter.outbound.jpa.entity.OrderItemEntity;
import app.giftify.funding.domain.OrderStatus;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.TargetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(OrderTestApplication.class)
@ActiveProfiles("local")
class OrderRepositoryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    @DisplayName("주문과 주문 아이템을 저장하고 조회할 수 있다")
    void saveAndFind() {
        // given
        OrderEntity orderEntity = OrderEntity.builder()
                .orderNumber("ORD-REPOSITORY-TEST")
                .buyerId(1L)
                .totalAmount(BigDecimal.valueOf(20000))
                .paymentMethod(PaymentMethod.CARD)
                .status(OrderStatus.PAYMENT_PENDING)
                .build();

        OrderEntity savedOrder = orderRepository.save(orderEntity);

        OrderItemEntity itemEntity = OrderItemEntity.builder()
                .orderId(savedOrder.getId())
                .targetSnapshotId(100L)
                .targetType(TargetType.PRODUCT)
                .sellerId(200L)
                .receiverId(300L)
                .price(BigDecimal.valueOf(10000))
                .quantity(2)
                .status(OrderStatus.PAYMENT_PENDING)
                .build();

        orderItemRepository.save(itemEntity);

        // when
        OrderEntity foundOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
        List<OrderItemEntity> foundItems = orderItemRepository.findByOrderId(savedOrder.getId());

        // then
        assertThat(foundOrder.getOrderNumber()).isEqualTo("ORD-REPOSITORY-TEST");
        assertThat(foundItems).hasSize(1);
        assertThat(foundItems.get(0).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(10000));
    }
}
