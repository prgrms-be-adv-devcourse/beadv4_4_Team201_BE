package app.giftify.product;

import app.giftify.security.common.config.SharedSecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        exclude = {
                SecurityAutoConfiguration.class,
                SharedSecurityAutoConfiguration.class
        }
)
@ComponentScan(
        basePackages = {
                "app.giftify.product",
                "app.giftify.replica",
                "app.giftify.support.jpa"
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = ProductJpaTestApplication.class
        )
)
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {
        "app.giftify.product",
        "app.giftify.replica",
        "app.giftify.support.jpa"
})
@EnableElasticsearchRepositories(basePackages = "app.giftify.product")
@EntityScan(basePackages = {
        "app.giftify.product",
        "app.giftify.replica",
        "app.giftify.support.jpa"
})
public class ProductEsTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductEsTestApplication.class, args);
    }
}
