package app.giftify.product;

import app.giftify.product.adapter.outbound.client.ProductApiClientConfig;
import app.giftify.product.application.port.in.ProductResult;
import app.giftify.product.application.port.out.ProductEsPort;
import app.giftify.product.application.port.out.ProductEsSearchCommand;
import app.giftify.product.domain.Product;
import app.giftify.security.common.config.SharedSecurityAutoConfiguration;
import app.giftify.support.common.api.paging.PageResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

@SpringBootApplication(
        exclude = {
                SharedSecurityAutoConfiguration.class
        },
        excludeName = {
                "org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration",
                "org.springframework.boot.elasticsearch.autoconfigure.ElasticsearchClientAutoConfiguration",
                "org.springframework.boot.elasticsearch.autoconfigure.ElasticsearchRestClientAutoConfiguration",
                "org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchAutoConfiguration",
                "org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchRepositoriesAutoConfiguration"
        }
)
@ComponentScan(
        basePackages = {
                "app.giftify.product",
                "app.giftify.support.jpa"
        },
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = ".*\\.elasticsearch\\..*"
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {ProductEsTestApplication.class, ProductApiClientConfig.class}
                )
        }
)
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {
        "app.giftify.product",
        "app.giftify.support.jpa"
})
@EntityScan(basePackages = {
        "app.giftify.product",
        "app.giftify.support.jpa"
})
public class ProductJpaTestApplication {

    @Bean
    ProductEsPort noOpProductEsPort() {
        return new ProductEsPort() {
            @Override
            public void save(Product product) {
            }

            @Override
            public int syncAll() {
                return 0;
            }

            @Override
            public PageResponse<ProductResult> searchProducts(ProductEsSearchCommand command) {
                return PageResponse.of(List.of(), 0, 20, 0);
            }

            @Override
            public void deleteById(Long productId) {
            }

            @Override
            public void updateSellerNickname(Long sellerId, String nickname) {
            }
        };
    }

    @Bean
    app.giftify.product.adapter.outbound.client.FundingApiClient fundingApiClient() {
        return productId -> false;
    }

    public static void main(String[] args) {
        SpringApplication.run(ProductJpaTestApplication.class, args);
    }
}
