package app.giftify.auth.adapter.outbound.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;

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

        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory(baseUrl);
        uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        RestClient restClient = builder.uriBuilderFactory(uriFactory).build();

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

    @Bean
    public Auth0ApiClient auth0Api(
            RestClient.Builder builder,
            @Value("${spring.security.oauth2.client.provider.auth0.issuer-uri}") String issuerUri) {

        RestClient restClient = builder.baseUrl(issuerUri).build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(Auth0ApiClient.class);
    }
}
