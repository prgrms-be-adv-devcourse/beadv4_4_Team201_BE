package app.giftify.order.application.service;

import app.giftify.order.application.port.out.OrderQueryPort;
import app.giftify.order.domain.domain.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class OrderGetServiceTest {

    private OrderQueryPort orderQueryPort;
    private OrderGetService orderGetService;

    @BeforeEach
    void setUp() {
        orderQueryPort = Mockito.mock(OrderQueryPort.class);
        orderGetService = new OrderGetService(orderQueryPort);
    }

    @Test
    @DisplayName("주문 ID로 주문 조회 성공")
    void getOrder_Success() {
        Long orderId = 1L;
        Order order = Mockito.mock(Order.class);
        when(orderQueryPort.findById(orderId)).thenReturn(Optional.of(order));

        Order result = orderGetService.getOrder(orderId);

        assertEquals(order, result);
    }

    @Test
    @DisplayName("주문 ID로 주문 조회 실패 - 존재하지 않음")
    void getOrder_Fail_NotFound() {
        Long orderId = 1L;
        when(orderQueryPort.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderGetService.getOrder(orderId));
    }

    @Test
    @DisplayName("판매자 ID로 주문 목록 조회")
    void getOrdersBySeller_Test() {
        Long sellerId = 100L;
        List<Order> orders = List.of(Mockito.mock(Order.class));
        when(orderQueryPort.findBySellerId(sellerId)).thenReturn(orders);

        List<Order> result = orderGetService.getOrdersBySeller(sellerId);

        assertEquals(orders, result);
    }

    @Test
    @DisplayName("구매자 ID로 주문 목록 조회")
    void getOrdersByBuyer_Test() {
        Long buyerId = 200L;
        List<Order> orders = List.of(Mockito.mock(Order.class));
        when(orderQueryPort.findByBuyerId(buyerId)).thenReturn(orders);

        List<Order> result = orderGetService.getOrdersByBuyer(buyerId);

        assertEquals(orders, result);
    }
}
