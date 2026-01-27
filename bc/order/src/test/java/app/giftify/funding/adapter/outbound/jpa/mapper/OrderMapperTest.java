package app.giftify.funding.adapter.outbound.jpa.mapper;

import app.giftify.funding.adapter.outbound.jpa.entity.OrderEntity;
import app.giftify.funding.domain.Order;
import app.giftify.funding.domain.OrderStatus;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.funding.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    @Test
    @DisplayName("도메인 모델을 엔티티로 변환한다")
    void toEntity() {
        // given
        Order order = Order.builder()
                .orderNumber("ORD-1234567890")
                .buyerId(1L)
                .totalAmount(Money.of(20000L))
                .paymentMethod(PaymentMethod.CARD)
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        // when
        OrderEntity entity = OrderMapper.toEntity(order);

        // then
        assertThat(entity.getOrderNumber()).isEqualTo(order.getOrderNumber());
        assertThat(entity.getBuyerId()).isEqualTo(order.getBuyerId());
        assertThat(entity.getTotalAmount()).isEqualByComparingTo(order.getTotalAmount().amount());
        assertThat(entity.getPaymentMethod()).isEqualTo(order.getPaymentMethod());
        assertThat(entity.getStatus()).isEqualTo(order.getStatus());
    }

    @Test
    @DisplayName("엔티티를 도메인 모델로 변환한다")
    void toDomain() {
        // given
        OrderEntity entity = OrderEntity.builder()
                .orderNumber("ORD-1234567890")
                .buyerId(1L)
                .totalAmount(java.math.BigDecimal.valueOf(20000))
                .paymentMethod(PaymentMethod.CARD)
                .status(OrderStatus.PAYMENT_PENDING)
                .build();
        // BaseJpaEntity의 id와 createdAt은 reflection이나 상속받은 setter가 없으면 direct 주입이 어려울 수 있으나 
        // OrderMapper.toDomain은 id와 createdAt도 매핑함.

        // when
        Order order = OrderMapper.toDomain(entity);

        // then
        assertThat(order.getOrderNumber()).isEqualTo(entity.getOrderNumber());
        assertThat(order.getBuyerId()).isEqualTo(entity.getBuyerId());
        assertThat(order.getTotalAmount().amount()).isEqualByComparingTo(entity.getTotalAmount());
        assertThat(order.getPaymentMethod()).isEqualTo(entity.getPaymentMethod());
        assertThat(order.getStatus()).isEqualTo(entity.getStatus());
    }
}
