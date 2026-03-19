package app.giftify.funding.adapter.outbound;

import app.giftify.funding.application.outbound.WishlistItemSnapshotApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class FundingApiClientConfig {
    @Bean
    public WishlistItemSnapshotApiClient wishlistItemSnapshotApiClient(
            RestClient.Builder builder,
            @Value("${app.service.catalog.url:http://localhost:8080}") String baseUrl
    ) {
        RestClient restClient = builder
                .baseUrl(baseUrl)
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(WishlistItemSnapshotApiClient.class);
    }
}
