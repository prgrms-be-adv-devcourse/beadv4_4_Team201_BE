package app.giftify.funding.application.service;

import app.giftify.funding.application.outbound.OrderRepositoryPort;
import app.giftify.funding.domain.Order;
import app.giftify.funding.domain.OrderStatus;
import app.giftify.funding.domain.exception.OrderException;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentResultServiceTest {

    @InjectMocks
    private PaymentResultService paymentResultService;

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Test
    @DisplayName("결제 성공 처리 시 주문 상태를 ORDERED로 변경하고 저장한다")
    void completePayment_success() {
        // given
        Long orderId = 1L;
        String paymentKey = "test-payment-key";
        Order order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-123456")
                .buyerId(100L)
                .totalAmount(Money.of(10000))
                .paymentMethod(PaymentMethod.CARD)
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));

        // when
        paymentResultService.completePayment(orderId, paymentKey);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaymentKey()).isEqualTo(paymentKey);
        verify(orderRepositoryPort).save(order);
    }

    @Test
    @DisplayName("존재하지 않는 주문의 결제 성공 처리 시 예외가 발생한다")
    void completePayment_fail_notFound() {
        // given
        Long orderId = 1L;
        given(orderRepositoryPort.findById(orderId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentResultService.completePayment(orderId, "key"))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("찾을 수 없는 주문입니다");
    }
    @Test
    @DisplayName("환불 처리 시 주문 상태를 REFUNDED로 변경하고 저장한다")
    void refundPayment_success() {
        // given
        Long orderId = 1L;
        Order order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-123456")
                .buyerId(100L)
                .totalAmount(Money.of(10000))
                .paymentMethod(PaymentMethod.CARD)
                .status(OrderStatus.PAID)
                .createdAt(LocalDateTime.now())
                .build();

        given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));

        // when
        paymentResultService.refundPayment(orderId);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
        verify(orderRepositoryPort).save(order);
    }

    @Test
    @DisplayName("존재하지 않는 주문의 환불 처리 시 예외가 발생한다")
    void refundPayment_fail_notFound() {
        // given
        Long orderId = 1L;
        given(orderRepositoryPort.findById(orderId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentResultService.refundPayment(orderId))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("찾을 수 없는 주문입니다");
    }

    @Test
    @DisplayName("결제 실패(재시도용) 처리 시 주문 상태를 FAILED로 변경하고 저장한다")
    void failPayment_success() {
        // given
        Long orderId = 1L;
        Order order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-123456")
                .buyerId(100L)
                .totalAmount(Money.of(10000))
                .paymentMethod(PaymentMethod.CARD)
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));

        // when
        paymentResultService.failPayment(orderId);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
        verify(orderRepositoryPort).save(order);
    }

    @Test
    @DisplayName("결제 취소 처리 시 주문 상태를 CANCELED로 변경하고 저장한다")
    void cancelPayment_success() {
        // given
        Long orderId = 1L;
        Order order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-123456")
                .buyerId(100L)
                .totalAmount(Money.of(10000))
                .paymentMethod(PaymentMethod.CARD)
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));

        // when
        paymentResultService.cancelPayment(orderId);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        verify(orderRepositoryPort).save(order);
    }
}
