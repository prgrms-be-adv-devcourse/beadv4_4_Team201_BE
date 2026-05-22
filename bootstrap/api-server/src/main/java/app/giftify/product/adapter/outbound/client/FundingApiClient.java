package app.giftify.product.adapter.outbound.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "/api/internal/funding")
public interface FundingApiClient {

    @GetExchange("/{productId}/exists")
    boolean checkFundingExistsByProductId(@PathVariable Long productId);
}
