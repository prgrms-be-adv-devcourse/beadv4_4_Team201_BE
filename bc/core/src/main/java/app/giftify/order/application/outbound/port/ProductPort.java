package app.giftify.order.application.outbound.port;

import app.giftify.shared.domain.vo.ProductSnapshot;

import java.util.List;
import java.util.Map;

public interface ProductPort {
    Map<Long, ProductSnapshot> getProductSnapshots(List<Long> productIds);
}
