package app.giftify.product.adapter.inbound.web.controller;

import app.giftify.product.application.port.in.GetProductSnapshotUseCase;
import app.giftify.shared.domain.vo.ProductSnapshot;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/products")
@RequiredArgsConstructor
public class InternalProductController {

    private final GetProductSnapshotUseCase getProductSnapshotUseCase;

    @PostMapping("/snapshots")
    public Map<Long, ProductSnapshot> getSnapshotList(
            @RequestBody @NotEmpty List<Long> productIds
    ) {
        return getProductSnapshotUseCase.getSnapshots(productIds);
    }
}
