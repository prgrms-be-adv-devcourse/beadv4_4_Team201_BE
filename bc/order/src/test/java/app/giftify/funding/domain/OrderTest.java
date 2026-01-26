package app.giftify.funding.domain;

import app.giftify.funding.domain.exception.OrderException;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.funding.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    @DisplayName("주문 생성 시 초기 상태 및 필드 값이 올바르게 설정된다")
    void createOrder() {
        // given
        String orderNumber = Order.generateOrderNumber();
        Long buyerId = 1L;
        Money totalAmount = Money.of(10000);
        PaymentMethod paymentMethod = PaymentMethod.CARD;
        LocalDateTime now = LocalDateTime.now();

        // when
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .buyerId(buyerId)
                .totalAmount(totalAmount)
                .paymentMethod(paymentMethod)
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(now)
                .build();

        // then
        assertThat(order.getOrderNumber()).isEqualTo(orderNumber);
        assertThat(order.getBuyerId()).isEqualTo(buyerId);
        assertThat(order.getTotalAmount()).isEqualTo(totalAmount);
        assertThat(order.getPaymentMethod()).isEqualTo(paymentMethod);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(order.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("결제 수단이 없으면 주문 생성에 실패한다")
    void createOrder_fail_noPaymentMethod() {
        assertThatThrownBy(() -> Order.builder()
                .orderNumber(Order.generateOrderNumber())
                .buyerId(1L)
                .totalAmount(Money.of(10000))
                .paymentMethod(null)
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now())
                .build())
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("결제 수단은 필수입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ORD", "short", "invalid!", "too_long_order_number_over_sixty_four_characters_long_12345678901234567890"})
    @DisplayName("유효하지 않은 주문 번호 형식은 주문 생성에 실패한다")
    void createOrder_fail_invalidOrderNumber(String invalidOrderNumber) {
        assertThatThrownBy(() -> Order.builder()
                .orderNumber(invalidOrderNumber)
                .buyerId(1L)
                .totalAmount(Money.of(10000))
                .paymentMethod(PaymentMethod.CARD)
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now())
                .build())
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("주문 번호는 영문 대소문자, 숫자, -, _로 구성된 6자 이상 64자 이하의 문자열이어야 합니다.");
    }

    @Test
    @DisplayName("주문 대기 상태에서 결제 완료 상태로 변경할 수 있다")
    void toOrdered() {
        // given
        Order order = createPendingOrder();
        String paymentKey = "payment-key-123";

        // when
        order.toOrdered(paymentKey);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaymentKey()).isEqualTo(paymentKey);
    }

    @Test
    @DisplayName("주문 대기 상태가 아닐 때 결제 완료 처리를 시도하면 예외가 발생한다")
    void toOrdered_fail_invalidStatus() {
        // given
        Order order = createPendingOrder();
        order.toOrdered("key");
        order.toConfirmed();

        // when & then
        assertThatThrownBy(() -> order.toOrdered("new-key"))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("주문 대기 상태에서만 결제가 가능합니다.");
    }

    @Test
    @DisplayName("주문 결제 완료 상태에서 주문 확정 상태로 변경할 수 있다")
    void toConfirmed() {
        // given
        Order order = createPendingOrder();
        order.toOrdered("payment-key");

        // when
        order.toConfirmed();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getConfirmedAt()).isNotNull();
    }

    @Test
    @DisplayName("주문 결제 완료 상태가 아닐 때 주문 확정 처리를 시도하면 예외가 발생한다")
    void toConfirmed_fail_invalidStatus() {
        // given
        Order order = createPendingOrder();

        // when & then
        assertThatThrownBy(order::toConfirmed)
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("주문 결제 완료 상태에서만 확정이 가능합니다.");
    }

    @Test
    @DisplayName("확정되지 않은 주문은 취소할 수 있다")
    void toCancelled() {
        // given
        Order order = createPendingOrder();

        // when
        order.toCancelled();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(order.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 확정된 주문은 취소할 수 없으며 예외가 발생한다")
    void toCancelled_fail_alreadyConfirmed() {
        // given
        Order order = createPendingOrder();
        order.toOrdered("key");
        order.toConfirmed();

        // when & then
        assertThatThrownBy(order::toCancelled)
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("이미 확정된 주문은 취소할 수 없습니다.");
    }

    @Test
    @DisplayName("주문 대기 상태이고 생성 시간이 경과하면 자동 취소가 가능하다")
    void canAutoCancel_true() {
        // given
        Order order = Order.builder()
                .orderNumber(Order.generateOrderNumber())
                .buyerId(1L)
                .totalAmount(Money.of(10000))
                .paymentMethod(PaymentMethod.CARD)
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now().minusMinutes(31))
                .build();

        // when
        boolean canCancel = order.canAutoCancel(30);

        // then
        assertThat(canCancel).isTrue();
    }

    @Test
    @DisplayName("주문 대기 상태가 아니면 자동 취소가 불가능하다")
    void canAutoCancel_false_invalidStatus() {
        // given
        Order order = Order.builder()
                .orderNumber(Order.generateOrderNumber())
                .buyerId(1L)
                .totalAmount(Money.of(10000))
                .paymentMethod(PaymentMethod.CARD)
                .status(OrderStatus.PAID)
                .createdAt(LocalDateTime.now().minusMinutes(31))
                .build();

        // when
        boolean canCancel = order.canAutoCancel(30);

        // then
        assertThat(canCancel).isFalse();
    }

    @Test
    @DisplayName("생성 시간이 경과하지 않았으면 자동 취소가 불가능하다")
    void canAutoCancel_false_notExpired() {
        // given
        Order order = Order.builder()
                .orderNumber(Order.generateOrderNumber())
                .buyerId(1L)
                .totalAmount(Money.of(10000))
                .paymentMethod(PaymentMethod.CARD)
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now().minusMinutes(29))
                .build();

        // when
        boolean canCancel = order.canAutoCancel(30);

        // then
        assertThat(canCancel).isFalse();
    }

    @Test
    @DisplayName("결제 완료 상태의 주문은 환불 상태로 변경할 수 있다")
    void toRefunded_success() {
        // given
        Order order = createPendingOrder();
        order.toOrdered("payment-key");

        // when
        order.toRefunded();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    @DisplayName("이미 확정된 주문은 환불할 수 없으며 예외가 발생한다")
    void toRefunded_fail_alreadyConfirmed() {
        // given
        Order order = createPendingOrder();
        order.toOrdered("key");
        order.toConfirmed();

        // when & then
        assertThatThrownBy(order::toRefunded)
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("이미 확정된 주문은 환불할 수 없습니다.");
    }

    @Test
    @DisplayName("결제 전 상태의 주문은 환불할 수 없으며 예외가 발생한다")
    void toRefunded_fail_notPaid() {
        // given
        Order order = createPendingOrder();

        // when & then
        assertThatThrownBy(order::toRefunded)
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("결제 이력이 없어 환불 가능한 상태가 아닙니다.");
    }

    private Order createPendingOrder() {
        return Order.builder()
                .orderNumber(Order.generateOrderNumber())
                .buyerId(1L)
                .totalAmount(Money.of(10000))
                .paymentMethod(PaymentMethod.CARD)
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
