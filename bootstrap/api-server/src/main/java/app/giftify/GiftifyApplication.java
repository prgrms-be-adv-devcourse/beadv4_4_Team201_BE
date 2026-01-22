package app.giftify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@ComponentScan(basePackages = {
    "app.giftify",
    "domain",
    "payment",
    "wallet",
    "walletHistory"
})
@EnableJpaRepositories(basePackages = {
    "app.giftify"
})
public class GiftifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(GiftifyApplication.class, args);
    }
}
