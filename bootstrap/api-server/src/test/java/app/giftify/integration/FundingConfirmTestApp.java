package app.giftify.integration;

import app.giftify.security.common.config.SharedSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(
        exclude = {SharedSecurityAutoConfiguration.class},
        excludeName = {
                "org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration",
                "org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration",
                "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration",
                "org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration",
                "org.springframework.boot.batch.autoconfigure.BatchAutoConfiguration",
                "org.springframework.modulith.runtime.autoconfigure.SpringModulithRuntimeAutoConfiguration",
                "org.springframework.boot.data.elasticsearch.autoconfigure.ElasticsearchClientAutoConfiguration",
                "org.springframework.boot.data.elasticsearch.autoconfigure.ElasticsearchRepositoriesAutoConfiguration",
                "org.springframework.boot.data.elasticsearch.autoconfigure.ReactiveElasticsearchClientAutoConfiguration"
        }
)
@ComponentScan(
        basePackages = {
                "app.giftify.funding",
                "app.giftify.order",
                "app.giftify.payment",
                "app.giftify.wallet",
                "app.giftify.facade",
                "app.giftify.product",
                "app.giftify.shared.config",
                "app.giftify.support.common",
                "app.giftify.support.jpa"
        },
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "app\\.giftify\\.product\\.adapter\\.outbound\\.elasticsearch\\..*"
                ),
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "app\\.giftify\\.support\\.common\\.config\\.RedisConfig"
                )
        }
)
@EnableJpaRepositories(basePackages = {
        "app.giftify",
        "org.springframework.modulith.events.jpa"
})
@EntityScan(basePackages = {
        "app.giftify",
        "org.springframework.modulith.events.jpa"
})
@EnableJpaAuditing
@EnableRetry
@EnableAsync
class FundingConfirmTestApp {
}