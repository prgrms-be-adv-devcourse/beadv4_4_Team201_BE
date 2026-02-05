package app.giftify.order.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.order.application.outbound.OrderRepositoryPort;
import app.giftify.order.domain.Order;
import app.giftify.order.domain.OrderStatus;
import app.giftify.order.domain.exception.OrderErrorCode;
import app.giftify.order.domain.exception.OrderException;
import app.giftify.order.domain.vo.Money;
import app.giftify.shared.domain.type.PaymentMethod;

@ExtendWith(MockitoExtension.class)
class PaymentResultServiceTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @InjectMocks
    private PaymentResultService paymentResultService;

    @Nested
    @DisplayName("결제 완료 처리")
    class CompletePayment {

        @Test
        @DisplayName("결제 대기 상태의 주문을 결제 완료 상태로 변경한다")
        void completePayment_Success() {
            // given
            Long orderId = 1L;
            String paymentKey = "payment-key-12345";
            Order order = createOrder(orderId, OrderStatus.PAYMENT_PENDING);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));
            given(orderRepositoryPort.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            paymentResultService.completePayment(orderId, paymentKey);

            // then
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepositoryPort).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();

            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(savedOrder.getPaymentKey()).isEqualTo(paymentKey);
        }

        @Test
        @DisplayName("주문이 존재하지 않으면 OrderException을 발생시킨다")
        void completePayment_OrderNotFound() {
            // given
            Long orderId = 999L;
            String paymentKey = "payment-key-12345";

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentResultService.completePayment(orderId, paymentKey))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("찾을 수 없는 주문입니다")
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_NOT_FOUND);

            verify(orderRepositoryPort).findById(orderId);
        }

        @Test
        @DisplayName("결제 대기 상태가 아닌 주문은 결제 완료 처리할 수 없다")
        void completePayment_NotPaymentPending() {
            // given
            Long orderId = 1L;
            String paymentKey = "payment-key-12345";
            Order order = createOrder(orderId, OrderStatus.PAID);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> paymentResultService.completePayment(orderId, paymentKey))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("주문 대기 상태에서만 결제가 가능합니다")
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_NOT_PAYABLE);
        }

        @Test
        @DisplayName("이미 취소된 주문은 결제 완료 처리할 수 없다")
        void completePayment_CanceledOrder() {
            // given
            Long orderId = 1L;
            String paymentKey = "payment-key-12345";
            Order order = createOrder(orderId, OrderStatus.CANCELED);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> paymentResultService.completePayment(orderId, paymentKey))
                    .isInstanceOf(OrderException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_NOT_PAYABLE);
        }

        @Test
        @DisplayName("이미 확정된 주문은 결제 완료 처리할 수 없다")
        void completePayment_ConfirmedOrder() {
            // given
            Long orderId = 1L;
            String paymentKey = "payment-key-12345";
            Order order = createOrder(orderId, OrderStatus.CONFIRMED);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> paymentResultService.completePayment(orderId, paymentKey))
                    .isInstanceOf(OrderException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_NOT_PAYABLE);
        }
    }

    @Nested
    @DisplayName("결제 환불 처리")
    class RefundPayment {

        @Test
        @DisplayName("결제 완료 상태의 주문을 환불 상태로 변경한다")
        void refundPayment_Success() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PAID);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));
            given(orderRepositoryPort.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            paymentResultService.refundPayment(orderId);

            // then
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepositoryPort).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();

            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.REFUNDED);
        }

        @Test
        @DisplayName("주문이 존재하지 않으면 OrderException을 발생시킨다")
        void refundPayment_OrderNotFound() {
            // given
            Long orderId = 999L;

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentResultService.refundPayment(orderId))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("찾을 수 없는 주문입니다")
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_NOT_FOUND);

            verify(orderRepositoryPort).findById(orderId);
        }

        @Test
        @DisplayName("이미 확정된 주문은 환불 처리할 수 없다")
        void refundPayment_ConfirmedOrder() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.CONFIRMED);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> paymentResultService.refundPayment(orderId))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("이미 확정된 주문은 환불할 수 없습니다")
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_ALREADY_CONFIRMED);
        }

        @Test
        @DisplayName("결제 이력이 없는 주문은 환불 처리할 수 없다")
        void refundPayment_NotPaidOrder() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PAYMENT_PENDING);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> paymentResultService.refundPayment(orderId))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("결제 이력이 없어 환불 가능한 상태가 아닙니다")
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_CANNOT_REFUND);
        }

        @Test
        @DisplayName("취소된 주문은 환불 처리할 수 없다")
        void refundPayment_CanceledOrder() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.CANCELED);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> paymentResultService.refundPayment(orderId))
                    .isInstanceOf(OrderException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_CANNOT_REFUND);
        }

        @Test
        @DisplayName("실패 상태의 주문은 환불 처리할 수 없다")
        void refundPayment_FailedOrder() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.FAILED);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> paymentResultService.refundPayment(orderId))
                    .isInstanceOf(OrderException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_CANNOT_REFUND);
        }
    }

    @Nested
    @DisplayName("결제 실패 처리")
    class FailPayment {

        @Test
        @DisplayName("결제 대기 상태의 주문을 실패 상태로 변경한다")
        void failPayment_Success_FromPaymentPending() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PAYMENT_PENDING);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));
            given(orderRepositoryPort.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            paymentResultService.failPayment(orderId);

            // then
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepositoryPort).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();

            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.FAILED);
        }

        @Test
        @DisplayName("결제 완료 상태의 주문을 실패 상태로 변경한다")
        void failPayment_Success_FromPaid() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PAID);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));
            given(orderRepositoryPort.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            paymentResultService.failPayment(orderId);

            // then
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepositoryPort).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();

            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.FAILED);
        }

        @Test
        @DisplayName("주문이 존재하지 않으면 OrderException을 발생시킨다")
        void failPayment_OrderNotFound() {
            // given
            Long orderId = 999L;

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentResultService.failPayment(orderId))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("찾을 수 없는 주문입니다")
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_NOT_FOUND);

            verify(orderRepositoryPort).findById(orderId);
        }

        @Test
        @DisplayName("이미 확정된 주문은 실패 처리할 수 없다")
        void failPayment_ConfirmedOrder() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.CONFIRMED);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> paymentResultService.failPayment(orderId))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("이미 확정된 주문은 취소할 수 없습니다")
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_ALREADY_CONFIRMED);
        }
    }

    @Nested
    @DisplayName("결제 취소 처리")
    class CancelPayment {

        @Test
        @DisplayName("결제 대기 상태의 주문을 취소 상태로 변경한다")
        void cancelPayment_Success_FromPaymentPending() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PAYMENT_PENDING);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));
            given(orderRepositoryPort.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            paymentResultService.cancelPayment(orderId);

            // then
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepositoryPort).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();

            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
            assertThat(savedOrder.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("결제 완료 상태의 주문을 취소 상태로 변경한다")
        void cancelPayment_Success_FromPaid() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.PAID);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));
            given(orderRepositoryPort.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            paymentResultService.cancelPayment(orderId);

            // then
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepositoryPort).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();

            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
            assertThat(savedOrder.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("실패 상태의 주문을 취소 상태로 변경한다")
        void cancelPayment_Success_FromFailed() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.FAILED);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));
            given(orderRepositoryPort.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            paymentResultService.cancelPayment(orderId);

            // then
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepositoryPort).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();

            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
            assertThat(savedOrder.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("주문이 존재하지 않으면 OrderException을 발생시킨다")
        void cancelPayment_OrderNotFound() {
            // given
            Long orderId = 999L;

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentResultService.cancelPayment(orderId))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("찾을 수 없는 주문입니다")
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_NOT_FOUND);

            verify(orderRepositoryPort).findById(orderId);
        }

        @Test
        @DisplayName("이미 확정된 주문은 취소 처리할 수 없다")
        void cancelPayment_ConfirmedOrder() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.CONFIRMED);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> paymentResultService.cancelPayment(orderId))
                    .isInstanceOf(OrderException.class)
                    .hasMessageContaining("이미 확정된 주문은 취소할 수 없습니다")
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_ALREADY_CONFIRMED);
        }
    }

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCases {

        @Test
        @DisplayName("null paymentKey로 결제 완료 처리가 가능하다")
        void completePayment_WithNullPaymentKey() {
            // given
            Long orderId = 1L;
            String paymentKey = null;
            Order order = createOrder(orderId, OrderStatus.PAYMENT_PENDING);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));
            given(orderRepositoryPort.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            paymentResultService.completePayment(orderId, paymentKey);

            // then
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepositoryPort).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();

            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(savedOrder.getPaymentKey()).isNull();
        }

        @Test
        @DisplayName("빈 문자열 paymentKey로 결제 완료 처리가 가능하다")
        void completePayment_WithEmptyPaymentKey() {
            // given
            Long orderId = 1L;
            String paymentKey = "";
            Order order = createOrder(orderId, OrderStatus.PAYMENT_PENDING);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));
            given(orderRepositoryPort.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            paymentResultService.completePayment(orderId, paymentKey);

            // then
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepositoryPort).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();

            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(savedOrder.getPaymentKey()).isEmpty();
        }

        @Test
        @DisplayName("환불된 주문은 다시 환불 처리할 수 없다")
        void refundPayment_AlreadyRefunded() {
            // given
            Long orderId = 1L;
            Order order = createOrder(orderId, OrderStatus.REFUNDED);

            given(orderRepositoryPort.findById(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> paymentResultService.refundPayment(orderId))
                    .isInstanceOf(OrderException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_CANNOT_REFUND);
        }
    }

    /**
     * 테스트용 Order 객체 생성 헬퍼 메서드
     */
    private Order createOrder(Long orderId, OrderStatus status) {
        return Order.builder()
                .id(orderId)
                .orderNumber("ORD-TEST123-20260202")
                .buyerId(1L)
                .totalAmount(Money.of(BigDecimal.valueOf(10000)))
                .paymentMethod(PaymentMethod.CARD)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
