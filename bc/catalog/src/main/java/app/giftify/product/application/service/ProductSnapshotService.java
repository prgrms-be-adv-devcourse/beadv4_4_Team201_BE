package app.giftify.product.application.service;

import app.giftify.product.application.port.in.GetProductSnapshotUseCase;
import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.Product;
import app.giftify.shared.domain.vo.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSnapshotService implements GetProductSnapshotUseCase {

    private final ProductSupport productSupport;

    @Override
    @Transactional(readOnly = true)
    public Map<Long, ProductSnapshot> getSnapshots(List<Long> productIds) {

        List<Product> productList = productSupport.findAllById(productIds);
        productSupport.validatePurchasable(productList); // 상품 검증 (ACTIVE && 재고!=0)

        return productList.stream()
                .collect(Collectors.toMap(
                        Product::getId,
                        p -> new ProductSnapshot(p.getId(), p.getPrice(), p.getSellerId())
                ));
    }
}
