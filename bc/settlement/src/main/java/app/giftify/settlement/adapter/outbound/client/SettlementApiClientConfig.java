package app.giftify.settlement.adapter.outbound.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class SettlementApiClientConfig {

    @Value("${internal-api.base-url}")
    private String baseUrl;

    @Bean
    public OrderClient orderClient(RestClient.Builder builder) {
        RestClient restClient = builder
                .baseUrl(baseUrl)
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(OrderClient.class);
    }
    @Bean
    public PaymentClient paymentClient(RestClient.Builder builder) {
        RestClient restClient = builder
                .baseUrl(baseUrl)
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(PaymentClient.class);
    }
}
