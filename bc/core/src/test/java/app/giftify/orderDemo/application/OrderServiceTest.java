package app.giftify.orderDemo.application;

import app.giftify.orderDemo.application.inbound.command.CreateOrderCommand;
import app.giftify.orderDemo.application.inbound.command.CreateOrderItemCommand;
import app.giftify.orderDemo.application.inbound.vo.OrderSummary;
import app.giftify.orderDemo.application.inbound.vo.PaymentSnapshot;
import app.giftify.orderDemo.application.outbound.port.OrderRepository;
import app.giftify.orderDemo.domain.Order;
import app.giftify.orderDemo.domain.OrderItem;
import app.giftify.orderDemo.domain.OrderSnapshot;
import app.giftify.orderDemo.domain.OrderStatus;
import app.giftify.orderDemo.domain.errorCode.OrderErrorCode;
import app.giftify.orderDemo.domain.fixture.OrderFixture;
import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.order.OrderCreatedEvent;
import app.giftify.shared.domain.event.order.OrderItemCreatedEvent;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = new CreateOrderCommand(
                1L,
                PaymentMethod.DEPOSIT,
                List.of(new CreateOrderItemCommand(
                        2L,
                        3L,
                        Money.of("1000"),
                        OrderItemType.FUNDING_GIFT,
                        TargetType.FUNDING,
                        4L,
                        Money.of("10000")
                ))
        );
    }

    @Test
    @DisplayName("주문 생성 성공")
    void placeOrder_success() {
        List<OrderItem> orderItems = validCommand.items().stream()
                .map(item -> OrderItem.create(
                        item.targetId(),
                        item.targetType(),
                        item.orderItemType(),
                        item.sellerId(),
                        item.receiverId(),
                        item.price(),
                        item.amount()
                ))
                .toList();


        Order order = Order.create(
                validCommand.buyerId(),
                orderItems,
                validCommand.method()
        );

        given(orderRepository.save(any(Order.class))).willReturn(order);

        OrderSnapshot snapshot = orderService.createOrder(validCommand);

        assertNotNull(snapshot);
        assertEquals(1, snapshot.orderItemSnapshots().size());
        assertEquals(Money.of(1000L), snapshot.totalAmount()); // price * amount
        assertEquals(OrderStatus.CREATED, snapshot.status());

        verify(eventPublisher, times(1)).publish(argThat(OrderCreatedEvent.class::isInstance));
        verify(eventPublisher, times(1)).publish(argThat(OrderItemCreatedEvent.class::isInstance));
    }

    @Test
    @DisplayName("주문 생성 실패 - price 0")
    void placeOrder_fail_zeroPrice() {
        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                PaymentMethod.DEPOSIT,
                List.of(new CreateOrderItemCommand(
                        2L,
                        3L,
                        Money.of("1000"),
                        OrderItemType.FUNDING_GIFT,
                        TargetType.FUNDING,
                        4L,
                        Money.zero()
                ))
        );

        assertThrows(DomainException.class,
                () -> orderService.createOrder(command));
    }

    @Test
    @DisplayName("주문 생성 실패 - amount 0")
    void placeOrder_fail_zeroAmount() {
        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                PaymentMethod.DEPOSIT,
                List.of(new CreateOrderItemCommand(
                        2L,
                        3L,
                        Money.zero(),
                        OrderItemType.FUNDING_GIFT,
                        TargetType.FUNDING,
                        4L,
                        Money.of("1000")
                ))
        );

        assertThrows(DomainException.class,
                () -> orderService.createOrder(command));
    }

    @Test
    @DisplayName("주문 생성 실패 - null buyerId")
    void placeOrder_fail_nullBuyer() {
        CreateOrderCommand command = new CreateOrderCommand(
                null,
                PaymentMethod.DEPOSIT,
                List.of(new CreateOrderItemCommand(
                        2L,
                        3L,
                        Money.of("1000"),
                        OrderItemType.FUNDING_GIFT,
                        TargetType.FUNDING,
                        4L,
                        Money.of("10000")
                ))
        );

        assertThrows(DomainException.class,
                () -> orderService.createOrder(command));
    }

    @Test
    @DisplayName("주문 생성 실패 - repository save 실패")
    void placeOrder_fail_repositoryError() {
        given(orderRepository.save(any(Order.class))).willThrow(new RuntimeException("DB Error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(validCommand));

        assertEquals("DB Error", ex.getMessage());
    }

    @Test
    void getOrders_shouldReturnMappedOrderViews() {
        // Given
        Long memberId = 1L;
        Pageable pageable = PageRequest.of(0, 2);

        // 샘플 Order 엔티티 생성
        Order order1 = mock(Order.class);
        Order order2 = mock(Order.class);

        MockedStatic<OrderSummary> orderViewMock = Mockito.mockStatic(OrderSummary.class);

        // OrderView 반환용 가짜 OrderView
        OrderSummary view1 = new OrderSummary(
                1L,
                "order_number1",
                1L,
                Money.of("10000"),
                OrderStatus.CREATED,
                PaymentMethod.DEPOSIT,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        OrderSummary view2 = new OrderSummary(
                2L,
                "order_number2",
                2L,
                Money.of("20000"),
                OrderStatus.CREATED,
                PaymentMethod.DEPOSIT,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // OrderView.of(Order) 가 호출될 때도 필요하면 stubbing
        orderViewMock.when(() -> OrderSummary.of(order1)).thenReturn(view1);
        orderViewMock.when(() -> OrderSummary.of(order2)).thenReturn(view2);

        Page<Order> orderPage = new PageImpl<>(List.of(order1, order2), pageable, 2);

        when(orderRepository.getByBuyerId(memberId, pageable)).thenReturn(orderPage);

        // When
        Page<OrderSummary> result = orderService.getOrders(memberId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);

        assertThat(result.getContent().get(0)).isEqualTo(view1);
        assertThat(result.getContent().get(1)).isEqualTo(view2);

        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(2);

        verify(orderRepository, times(1)).getByBuyerId(memberId, pageable);
    }

    @Test
    @DisplayName("getOrders: 주문 없음 → 빈 Page 반환")
    void getOrders_shouldReturnEmptyPage_whenNoOrders() {
        // Given
        Long memberId = 1L;
        Pageable pageable = PageRequest.of(0, 1);
        Page<Order> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(orderRepository.getByBuyerId(memberId, pageable)).thenReturn(emptyPage);

        // When
        Page<OrderSummary> result = orderService.getOrders(memberId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty(); // 주문이 없으면 빈 리스트
        assertThat(result.getTotalElements()).isZero();
        verify(orderRepository, times(1)).getByBuyerId(memberId, pageable);
    }

    @Nested
    @DisplayName("결제 완료 처리 (markOrderAsPaid)")
    class MarkOrderAsPaid {

        @Test
        @DisplayName("성공: 주문을 조회하여 결제 완료 상태로 변경한다")
        void success() {
            // given
            Order order = spy(OrderFixture.createOrderWithItems(1L, 2)); // 엔티티 메서드 호출 확인을 위해 spy 사용

            String orderNumber = "orderNumber";

            PaymentSnapshot snapshot = new PaymentSnapshot(
                    1L,
                    "PG_KEY_123",
                    "TX_KEY_456",
                    LocalDateTime.now()
            );

            given(orderRepository.getByOrderNumber(orderNumber)).willReturn(order);

            // when
            orderService.markOrderAsPaid(orderNumber, snapshot);

            // then
            // 1. 레포지토리 조회가 정확한 주문번호로 이루어졌는지 확인
            verify(orderRepository).getByOrderNumber(orderNumber);

            // 2. 엔티티의 toPaid 메서드가 스냅샷의 데이터로 호출되었는지 확인
            verify(order).toPaid(snapshot.paymentKey(), snapshot.lastTransactionKey(), snapshot.createdAt());

            // 3. 실제 상태가 변했는지 확인 (더티 체킹에 의해 반영될 상태)
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 주문번호일 경우 예외가 발생한다")
        void fail_not_found() {
            // given
            PaymentSnapshot snapshot = new PaymentSnapshot(
                    1L,
                    "PG_KEY_123",
                    "TX_KEY_456",
                    LocalDateTime.now()
            );

            String orderNumber = "orderNumber";

            given(orderRepository.getByOrderNumber(orderNumber))
                    .willThrow(new PolicyException(OrderErrorCode.ORDER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> orderService.markOrderAsPaid(orderNumber, snapshot))
                    .isInstanceOf(PolicyException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);

            // 조회가 실패했으므로 엔티티 로직은 실행되지 않아야 함을 검증
            verify(orderRepository, never()).save(any());// save를 안 쓰는 구조라도 상호작용 없음 확인
        }

        @Test
        @DisplayName("실패: 취소된 주문 등 결제 불가능한 상태일 경우 PolicyException이 발생한다")
        void fail_invalid_status() {
            // given
            // 이미 취소된 주문 준비
            Order cancelledOrder = OrderFixture.createOrderWithStatus(OrderStatus.CANCELED);
            PaymentSnapshot snapshot = new PaymentSnapshot(
                    1L,
                    "PG_KEY_123",
                    "TX_KEY_456",
                    LocalDateTime.now()
            );

            String orderNumber = "orderNumber";

            given(orderRepository.getByOrderNumber(orderNumber)).willReturn(cancelledOrder);

            // when & then
            assertThatThrownBy(() -> orderService.markOrderAsPaid(orderNumber, snapshot))
                    .isInstanceOf(PolicyException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.INVALID_STATUS_TRANSITION);

            // 상태가 변하지 않았는지 확인
            assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
        }
    }
}