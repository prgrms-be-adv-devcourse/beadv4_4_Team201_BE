package app.giftify.product.adapter.inbound;

import app.giftify.product.application.port.in.GetProductSnapshotUseCase;
import app.giftify.shared.domain.port.ProductSnapshotPort;
import app.giftify.shared.domain.vo.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductSnapshotPortAdapter implements ProductSnapshotPort {

    private final GetProductSnapshotUseCase getProductSnapshotUseCase;

    @Override
    public Map<Long, ProductSnapshot> getProductSnapshots(List<Long> productIds) {
        return getProductSnapshotUseCase.getSnapshots(productIds);
    }
}
