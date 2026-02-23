package app.giftify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@ComponentScan(basePackages = {
        "app.giftify",
        "giftify.support.web",
        "domain",
        "payment",
        "wallet",
        "walletHistory"
})
@EnableJpaRepositories(basePackages = {
        "app.giftify"
})
@EnableElasticsearchRepositories(basePackages = "app.giftify")
@EnableRetry
@EnableAsync
@ConfigurationPropertiesScan
public class GiftifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(GiftifyApplication.class, args);
    }
}
