package app.giftify.product;

import app.giftify.product.adapter.outbound.client.ProductApiClientConfig;
import app.giftify.security.common.config.SharedSecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        exclude = {
                SharedSecurityAutoConfiguration.class
        },
        excludeName = {
                "org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration"
        }
)
@ComponentScan(
        basePackages = {
                "app.giftify.product",
                "app.giftify.support.jpa"
        },
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {ProductJpaTestApplication.class, ProductApiClientConfig.class}
                )
        }
)
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {
        "app.giftify.product",
        "app.giftify.support.jpa"
})
@EnableElasticsearchRepositories(basePackages = "app.giftify.product")
@EntityScan(basePackages = {
        "app.giftify.product",
        "app.giftify.support.jpa"
})
public class ProductEsTestApplication {

    @Bean
    app.giftify.product.adapter.outbound.client.FundingApiClient fundingApiClient() {
        return productId -> false;
    }

    public static void main(String[] args) {
        SpringApplication.run(ProductEsTestApplication.class, args);
    }
}
