package app.giftify.order.adapter.outbound.api;

import app.giftify.order.adapter.outbound.client.ProductClient;
import app.giftify.order.application.outbound.port.ProductPort;
import app.giftify.shared.domain.vo.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductApiAdapter implements ProductPort {

    private final ProductClient productClient;

    @Override
    public Map<Long, ProductSnapshot> getProductSnapshots(List<Long> productIds) {
        return productClient.getSnapshots(productIds);
    }
}
