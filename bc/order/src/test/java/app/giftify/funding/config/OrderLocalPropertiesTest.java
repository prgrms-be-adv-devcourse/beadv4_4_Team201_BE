package app.giftify.funding.config;

import app.giftify.funding.adapter.outbound.api.OrderPaymentAdapter;
import app.giftify.funding.domain.Order;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import app.giftify.shared.domain.type.PaymentMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class OrderLocalPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("custom.global.internalBackUrl=http://localhost:8080");

    @Test
    @DisplayName("local 프로파일 환경 설정값 custom.global.internalBackUrl이 정상적으로 로드된다")
    void testLocalInternalBackUrl() {
        contextRunner.run(context -> {
            String internalBackUrl = context.getEnvironment().getProperty("custom.global.internalBackUrl");
            assertThat(internalBackUrl).isEqualTo("http://localhost:8080");
        });
    }

    @Test
    @DisplayName("주입된 설정값을 사용하여 OrderPaymentAdapter가 정상적으로 동작한다")
    void testOrderPaymentAdapterWithLocalConfig() {
        contextRunner.run(context -> {
            String internalBackUrl = "http://localhost:8080";
            
            // Adapter 생성 및 RestClient 모킹
            OrderPaymentAdapter adapter = new OrderPaymentAdapter(internalBackUrl);
            RestClient.Builder builder = RestClient.builder();
            MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();
            ReflectionTestUtils.setField(adapter, "restClient", builder.build());

            Order order = Order.builder()
                    .id(1L)
                    .orderNumber("ORD-123")
                    .totalAmount(Money.of(10000))
                    .paymentMethod(PaymentMethod.CARD)
                    .build();

            // baseUrl(internalBackUrl + "/api") -> http://localhost:8080/api/payments/initiate
            mockServer.expect(requestTo("/payments/initiate"))
                    .andRespond(withStatus(HttpStatus.OK));

            // when
            adapter.initiatePayment(order);

            // then
            mockServer.verify();
        });
    }
}
