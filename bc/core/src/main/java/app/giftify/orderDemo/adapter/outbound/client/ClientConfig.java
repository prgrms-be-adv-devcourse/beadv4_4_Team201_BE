package app.giftify.orderDemo.adapter.outbound.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfig {

    @Value("${internal-api.base-url}")
    private String baseUrl;

    @Bean
    public WishlistClient wishlistClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(WishlistClient.class);
    }
}
