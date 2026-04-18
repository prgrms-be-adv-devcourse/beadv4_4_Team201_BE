package app.giftify.shared.domain.port;

import app.giftify.shared.domain.vo.ProductSnapshot;

import java.util.List;
import java.util.Map;

public interface ProductSnapshotPort {
    Map<Long, ProductSnapshot> getProductSnapshots(List<Long> productIds);
}
