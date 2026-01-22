package app.giftify.order.adapter.in.web.controller;

import app.giftify.order.adapter.in.web.dto.request.CreateOrderRequest;
import app.giftify.order.adapter.in.web.dto.request.OrderItemRequest;
import app.giftify.order.application.port.in.OrderUseCase;
import app.giftify.order.domain.domain.Order;
import app.giftify.order.domain.domain.OrderStatus;
import app.giftify.shared.domain.vo.Money;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@WithMockUser
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderUseCase orderUseCase;

    @Test
    @DisplayName("주문 생성 요청을 정상적으로 처리한다")
    void createOrder_Success() throws Exception {
        // given
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setFundingId(10L);
        itemRequest.setProductId(100L);
        itemRequest.setSellerId(1000L);
        itemRequest.setReceiverId(1L);
        itemRequest.setPrice(10000L);
        itemRequest.setQuantity(1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setBuyerId(1L);
        request.setItems(List.of(itemRequest));

        Order order = Order.builder()
                .id(1L)
                .orderNumber("ORD-123456")
                .buyerId(1L)
                .totalAmount(Money.of(10000))
                .status(OrderStatus.PAYMENT_PENDING)
                .build();

        when(orderUseCase.createOrder(any())).thenReturn(order);

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.orderNumber").value("ORD-123456"))
                .andExpect(jsonPath("$.status").value("PAYMENT_PENDING"));
        
        verify(orderUseCase).createOrder(any());
    }

    @Test
    @DisplayName("주문 아이템 확정 요청을 정상적으로 처리한다")
    void confirmOrderItem_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/orders/1/items/10/confirm")
                        .with(csrf())
                        .param("receiverId", "5"))
                .andExpect(status().isOk());

        verify(orderUseCase).confirmOrderItem(any());
    }

    @Test
    @DisplayName("주문 취소 요청을 정상적으로 처리한다")
    void cancelOrder_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/orders/1/cancel")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(orderUseCase).cancelOrder(any());
    }
}
