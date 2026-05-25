package app.giftify.product.domain.port;

import app.giftify.product.domain.vo.ProductSnapshot;

import java.util.List;
import java.util.Map;

public interface ProductSnapshotPort {
    Map<Long, ProductSnapshot> getProductSnapshots(List<Long> productIds);
}
