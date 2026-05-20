package app.giftify.product.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.product.application.port.in.GetProductSnapshotUseCase;
import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.shared.domain.vo.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductSnapshotService implements GetProductSnapshotUseCase {
	private static final Logger log = LoggerFactory.getLogger(ProductSnapshotService.class);


    private final ProductSupport productSupport;

    @Override
    @Transactional(readOnly = true)
    public Map<Long, ProductSnapshot> getSnapshots(List<Long> productIds) {

        List<Product> productList = productSupport.findAllById(productIds);

        if (productList.size() != productIds.size()) {
            List<Long> foundIds = productList.stream().map(Product::getId).toList();
            List<Long> missingIds = productIds.stream().filter(id -> !foundIds.contains(id)).toList();
            log.warn("[ProductSnapshot] 존재하지 않는 상품 요청. missingIds={}", missingIds);
        }

        return productList.stream()
                .collect(Collectors.toMap(
                        Product::getId,
                        p -> new ProductSnapshot(
                                p.getId(),
                                p.getPrice(),
                                p.getSellerId(),
                                p.getStatus() == ProductStatus.ACTIVE && p.getStock() > 0
                        )
                ));
    }
}
