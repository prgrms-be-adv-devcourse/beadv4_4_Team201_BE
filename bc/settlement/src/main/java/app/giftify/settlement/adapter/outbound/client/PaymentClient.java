package app.giftify.settlement.adapter.outbound.client;

import app.giftify.shared.api.exception.InfraException;
import app.giftify.shared.domain.vo.Money;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

@HttpExchange(url = "/api/internal/payments")
public interface PaymentClient {

    @Retryable(
            retryFor = InfraException.class,
            backoff = @Backoff(delay = 500)
    )
    @PostExchange(url = "/bulk-amounts")
    Map<Long, Money> getTotalAmounts(List<Long> orderIds);
}
