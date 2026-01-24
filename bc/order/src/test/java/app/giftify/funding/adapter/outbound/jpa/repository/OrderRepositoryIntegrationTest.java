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
                .build();

        orderItemRepository.save(itemEntity);

        // when
        OrderEntity foundOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
        List<OrderItemEntity> foundItems = orderItemRepository.findByOrderId(savedOrder.getId());

        // then
        assertThat(foundOrder.getOrderNumber()).isEqualTo("ORD-REPOSITORY-TEST");
        assertThat(foundItems).hasSize(1);
        assertThat(foundItems.get(0).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(foundOrder.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("상태 변경 시 updatedAt이 업데이트된다")
    void updateUpdatedAt() throws InterruptedException {
        // given
        OrderEntity orderEntity = OrderEntity.builder()
                .orderNumber("ORD-UPDATE-TEST")
                .buyerId(1L)
                .totalAmount(BigDecimal.valueOf(10000))
                .paymentMethod(PaymentMethod.CARD)
                .status(OrderStatus.PAYMENT_PENDING)
                .build();

        OrderEntity savedOrder = orderRepository.saveAndFlush(orderEntity);
        java.time.LocalDateTime initialUpdatedAt = savedOrder.getUpdatedAt();
        assertThat(initialUpdatedAt).isNotNull();

        // 약간의 시간 지연을 주어 updatedAt이 확실히 다르게 기록되도록 함
        Thread.sleep(100);

        // when
        // 직접 필드를 수정하면 Auditing이 동작하지 않을 수 있으므로, 
        // @AllArgsConstructor나 @Builder를 사용하지 않고 
        // 새로운 객체로 저장하거나 setter를 사용해야 함.
        // 현재 OrderEntity는 @Setter가 없으므로 Reflection을 쓰거나 Builder로 다시 만들어야 함.
        OrderEntity updatedEntity = OrderEntity.builder()
                .orderNumber(savedOrder.getOrderNumber())
                .buyerId(savedOrder.getBuyerId())
                .totalAmount(savedOrder.getTotalAmount())
                .paymentMethod(savedOrder.getPaymentMethod())
                .status(OrderStatus.ORDERED)
                .build();
        
        // ID를 명시적으로 설정하여 기존 엔티티를 업데이트하도록 함
        org.springframework.test.util.ReflectionTestUtils.setField(updatedEntity, "id", savedOrder.getId());
        
        OrderEntity finalSavedOrder = orderRepository.saveAndFlush(updatedEntity);

        // then
        assertThat(finalSavedOrder.getUpdatedAt()).isAfter(initialUpdatedAt);
    }
}
