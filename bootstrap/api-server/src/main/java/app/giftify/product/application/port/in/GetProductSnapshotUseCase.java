package app.giftify.product.application.port.in;

import app.giftify.product.domain.vo.ProductSnapshot;

import java.util.List;
import java.util.Map;

public interface GetProductSnapshotUseCase {
    Map<Long, ProductSnapshot> getSnapshots(List<Long> productIds);
}
