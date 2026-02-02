package app.giftify.auth.adapter.outbound.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Internal API 클라이언트 설정.
 * HTTP Interface를 RestClient 기반으로 프록시 생성합니다.
 */
@Configuration
public class ApiClientConfig {

    @Bean
    public MemberApiClient memberInternalApi(
            RestClient.Builder builder,
            @Value("${app.service.member.url:http://localhost:8080}") String baseUrl) {
        
        RestClient restClient = builder.baseUrl(baseUrl).build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(MemberApiClient.class);
    }

    @Bean
    public WalletApiClient walletInternalApi(
            RestClient.Builder builder,
            @Value("${app.service.wallet.url:http://localhost:8080}") String baseUrl) {
        
        RestClient restClient = builder.baseUrl(baseUrl).build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(WalletApiClient.class);
    }
}
