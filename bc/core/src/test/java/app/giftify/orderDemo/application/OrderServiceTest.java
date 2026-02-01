package app.giftify.orderDemo.application;

import app.giftify.orderDemo.application.inbound.command.CreateOrderCommand;
import app.giftify.orderDemo.application.inbound.command.CreateOrderItemCommand;
import app.giftify.orderDemo.application.outbound.port.OrderItemRepository;
import app.giftify.orderDemo.application.outbound.port.OrderRepository;
import app.giftify.orderDemo.domain.Order;
import app.giftify.orderDemo.domain.OrderItem;
import app.giftify.orderDemo.domain.OrderSnapshot;
import app.giftify.orderDemo.domain.OrderStatus;
import app.giftify.orderDemo.domain.exception.DomainException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.order.OrderCreatedEvent;
import app.giftify.shared.domain.event.order.OrderItemCreatedEvent;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.PaymentMethodType;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = new CreateOrderCommand(
                1L,
                PaymentMethodType.WALLET,
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

        verify(eventPublisher, times(1)).publish(argThat(event ->
                event instanceof OrderCreatedEvent));
        verify(eventPublisher, times(1)).publish(argThat(event ->
                event instanceof OrderItemCreatedEvent));
    }

    @Test
    @DisplayName("주문 생성 실패 - price 0")
    void placeOrder_fail_zeroPrice() {
        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                PaymentMethodType.WALLET,
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
                PaymentMethodType.WALLET,
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
                PaymentMethodType.WALLET,
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
}