package app.giftify.funding.adapter.outbound.api;

import app.giftify.funding.domain.Order;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.funding.domain.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class OrderPaymentAdapterTest {

    private OrderPaymentAdapter orderPaymentAdapter;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        
        orderPaymentAdapter = new OrderPaymentAdapter("http://localhost:8080");
        ReflectionTestUtils.setField(orderPaymentAdapter, "restClient", builder.build());
    }

    @Test
    @DisplayName("결제 요청 시 올바른 URL과 바디로 API를 호출한다")
    void initiatePayment_success() {
        // given
        Order order = Order.builder()
                .id(1L)
                .orderNumber("ORD-123")
                .totalAmount(Money.of(10000))
                .paymentMethod(PaymentMethod.CARD)
                .build();

        mockServer.expect(requestTo("/payments/initiate"))
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.amount").value(10000))
                .andRespond(withStatus(HttpStatus.OK));

        // when
        orderPaymentAdapter.initiatePayment(order);

        // then
        mockServer.verify();
    }

    @Test
    @DisplayName("결제 취소 요청 시 올바른 URL과 바디로 API를 호출한다")
    void cancelPayment_success() {
        // given
        String orderNumber = "ORD-123";

        mockServer.expect(requestTo("/payments/cancel"))
                .andExpect(jsonPath("$.orderNumber").value(orderNumber))
                .andRespond(withStatus(HttpStatus.OK));

        // when
        orderPaymentAdapter.cancelPayment(orderNumber);

        // then
        mockServer.verify();
    }
}
