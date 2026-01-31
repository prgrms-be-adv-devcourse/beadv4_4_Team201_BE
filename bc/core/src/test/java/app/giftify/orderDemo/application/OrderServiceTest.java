package app.giftify.orderDemo.application;

import app.giftify.orderDemo.application.inbound.command.PlaceOrderForItemCommand;
import app.giftify.orderDemo.application.outbound.port.OrderItemRepository;
import app.giftify.orderDemo.application.outbound.port.OrderRepository;
import app.giftify.orderDemo.domain.Order;
import app.giftify.orderDemo.domain.OrderItem;
import app.giftify.orderDemo.domain.OrderSnapshot;
import app.giftify.orderDemo.domain.OrderStatus;
import app.giftify.orderDemo.domain.exception.DomainException;
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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderService orderService;

    private PlaceOrderForItemCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = new PlaceOrderForItemCommand(
                1L,
                TargetType.PRODUCT,
                2L,
                3L,
                Money.of(1000L),
                PaymentMethodType.WALLET,
                1L,
                Money.of(10000L)
        );
    }

    @Test
    @DisplayName("주문 생성 성공")
    void placeOrder_success() {
        OrderItem item = OrderItem.create(
                validCommand.targetId(),
                validCommand.targetType(),
                validCommand.sellerId(),
                validCommand.receiverId(),
                validCommand.price(),
                validCommand.amount()
        );

        Order order = Order.create(
                validCommand.buyerId(),
                List.of(item),
                validCommand.method()
        );

        given(orderRepository.save(any(Order.class))).willReturn(order);

        OrderSnapshot snapshot = orderService.placeOrderForItem(validCommand);

        assertNotNull(snapshot);
        assertEquals(1, snapshot.orderItemSnapshots().size());
        assertEquals(Money.of(1000L), snapshot.totalAmount()); // price * amount
        assertEquals(OrderStatus.CREATED, snapshot.status());
    }

    @Test
    @DisplayName("주문 생성 실패 - price 0")
    void placeOrder_fail_zeroPrice() {
        PlaceOrderForItemCommand command = new PlaceOrderForItemCommand(
                1L,
                TargetType.PRODUCT,
                2L,
                3L,
                Money.zero(),
                PaymentMethodType.WALLET,
                1L,
                Money.of(10000L)
        );

        assertThrows(DomainException.class,
                () -> orderService.placeOrderForItem(command));
    }

    @Test
    @DisplayName("주문 생성 실패 - amount 0")
    void placeOrder_fail_zeroAmount() {
        PlaceOrderForItemCommand command = new PlaceOrderForItemCommand(
                1L,
                TargetType.PRODUCT,
                2L,
                3L,
                Money.of(1000L),
                PaymentMethodType.WALLET,
                1L,
                Money.zero()
        );

        assertThrows(DomainException.class,
                () -> orderService.placeOrderForItem(command));
    }

    @Test
    @DisplayName("주문 생성 실패 - null buyerId")
    void placeOrder_fail_nullBuyer() {
        PlaceOrderForItemCommand command = new PlaceOrderForItemCommand(
                1L,
                TargetType.PRODUCT,
                null,
                3L,
                Money.of(1000L),
                PaymentMethodType.WALLET,
                1L,
                Money.zero()
        );

        assertThrows(DomainException.class,
                () -> orderService.placeOrderForItem(command));
    }

    @Test
    @DisplayName("주문 생성 실패 - repository save 실패")
    void placeOrder_fail_repositoryError() {
        given(orderRepository.save(any(Order.class))).willThrow(new RuntimeException("DB Error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.placeOrderForItem(validCommand));

        assertEquals("DB Error", ex.getMessage());
    }
}