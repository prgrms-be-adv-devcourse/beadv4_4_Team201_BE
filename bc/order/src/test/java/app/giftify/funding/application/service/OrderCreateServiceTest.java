package app.giftify.funding.application.service;

import app.giftify.funding.adapter.inbound.web.dto.OrderCreateRequest;
import app.giftify.funding.adapter.inbound.web.dto.OrderResponse;
import app.giftify.funding.application.outbound.OrderItemRepositoryPort;
import app.giftify.funding.application.outbound.OrderPaymentPort;
import app.giftify.funding.application.outbound.OrderRepositoryPort;
import app.giftify.funding.domain.Order;
import app.giftify.funding.domain.OrderItem;
import app.giftify.funding.domain.OrderStatus;
import app.giftify.funding.domain.exception.OrderException;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCreateServiceTest {

    @InjectMocks
    private OrderCreateService orderCreateService;

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private OrderItemRepositoryPort orderItemRepositoryPort;

    @Mock
    private OrderPaymentPort orderPaymentPort;

    @Test
    @DisplayName("주문 생성 요청이 올바르면 주문과 주문 아이템을 생성하고 저장한다")
    void createOrder_success() {
        // given
        Long buyerId = 1L;
        PaymentMethod paymentMethod = PaymentMethod.CARD;
        OrderCreateRequest.OrderItemRequest itemRequest = new OrderCreateRequest.OrderItemRequest(
                100L, TargetType.PRODUCT, 200L, 300L, 10000L, 2
        );
        OrderCreateRequest request = new OrderCreateRequest(buyerId, paymentMethod, List.of(itemRequest));

        Order savedOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-20240124-1234567")
                .buyerId(buyerId)
                .totalAmount(Money.of(20000L))
                .paymentMethod(paymentMethod)
                .status(OrderStatus.PAYMENT_PENDING)
                .build();

        OrderItem savedItem = OrderItem.builder()
                .id(1L)
                .orderId(1L)
                .targetSnapshotId(100L)
                .targetType(TargetType.PRODUCT)
                .sellerId(200L)
                .receiverId(300L)
                .price(Money.of(10000L))
                .quantity(new app.giftify.shared.domain.vo.Quantity(2))
                .status(OrderStatus.PAYMENT_PENDING)
                .build();

        given(orderRepositoryPort.save(any(Order.class))).willReturn(savedOrder);
        given(orderItemRepositoryPort.saveAll(anyList())).willReturn(List.of(savedItem));

        // when
        OrderResponse response = orderCreateService.createOrder(request);

        // then
        assertThat(response.id()).isEqualTo(savedOrder.getId());
        assertThat(response.totalAmount()).isEqualTo(20000L);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).price()).isEqualTo(10000L);
        assertThat(response.items().get(0).quantity()).isEqualTo(2);

        verify(orderRepositoryPort).save(any(Order.class));
        verify(orderItemRepositoryPort).saveAll(anyList());
        verify(orderPaymentPort).initiatePayment(any(Order.class));
    }

    @Test
    @DisplayName("주문 저장에 실패하면 예외를 던진다")
    void createOrder_fail_orderSave() {
        // given
        OrderCreateRequest request = new OrderCreateRequest(
                1L, PaymentMethod.CARD, List.of(new OrderCreateRequest.OrderItemRequest(
                100L, TargetType.PRODUCT, 200L, 300L, 10000L, 2
        )));

        given(orderRepositoryPort.save(any(Order.class))).willReturn(null);

        // when & then
        assertThatThrownBy(() -> orderCreateService.createOrder(request))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("주문 생성에 실패했습니다.");
    }

    @Test
    @DisplayName("주문 아이템 저장에 실패하면 예외를 던진다")
    void createOrder_fail_orderItemSave() {
        // given
        OrderCreateRequest request = new OrderCreateRequest(
                1L, PaymentMethod.CARD, List.of(new OrderCreateRequest.OrderItemRequest(
                100L, TargetType.PRODUCT, 200L, 300L, 10000L, 2
        )));

        Order savedOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-123")
                .buyerId(1L)
                .totalAmount(Money.of(20000L))
                .paymentMethod(PaymentMethod.CARD)
                .status(OrderStatus.PAYMENT_PENDING)
                .build();

        given(orderRepositoryPort.save(any(Order.class))).willReturn(savedOrder);
        given(orderItemRepositoryPort.saveAll(anyList())).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> orderCreateService.createOrder(request))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("주문 아이템 생성에 실패했습니다.");
    }
}
