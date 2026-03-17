package app.giftify.order.adapter.outbound.client;

import app.giftify.shared.domain.vo.ProductSnapshot;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

@HttpExchange(url = "/api/internal/products")
public interface ProductClient {

    @PostExchange(url = "/snapshots")
    Map<Long, ProductSnapshot> getSnapshots(@RequestBody List<Long> productIds);
}
