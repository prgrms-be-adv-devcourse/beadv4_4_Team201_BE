package app.giftify.product.application;

import app.giftify.product.adapter.inbound.ProductUpdateRequestDto;
import app.giftify.product.adapter.inbound.ProductUpdateResponseDto;
import app.giftify.product.adapter.outbound.ProductRepository;
import app.giftify.product.adapter.outbound.ProductStockHistoryRepository;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStockHistory;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.product.ProductReplicaUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static app.giftify.product.domain.ProductStatus.ACTIVE;
import static app.giftify.product.domain.ProductStatus.INACTIVE;

@Service
@RequiredArgsConstructor
public class ProductUpdateUseCase {
    private final ProductSupport productSupport;
    private final EventPublisher eventPublisher;
    private final ProductStockHistoryRepository productStockHistoryRepository;
    private final ProductRepository productRepository;

    // todo @Lock
    public ProductUpdateResponseDto updateProduct(Long productId, Long sellerId, ProductUpdateRequestDto requestDto) {
        Product product = productSupport.findByIdAndSellerId(productId, sellerId);

        // 변경 전 값 저장 (더티체킹용)
        String oldName = product.getName();
        int oldPrice = product.getPrice();

        Optional.ofNullable(requestDto.name()).ifPresent(product::updateName);
        Optional.ofNullable(requestDto.description()).ifPresent(product::updateDescription);
        Optional.ofNullable(requestDto.price()).ifPresent(product::updatePrice);

        // 재고 수정, 재고 이력 저장
        Integer newStock = requestDto.stock();
        if (newStock != null && product.getStock() != newStock) {
            Product.StockChangeResult result = product.updateStock(newStock); // 재고 수정

            productRepository.save(product); // "변경"과 "이력"을 같은 트랜잭션에서 같이 저장하여 캡슐화

            ProductStockHistory history = ProductStockHistory.manualAdjust(
                    product.getSellerId(),
                    product.getId(),
                    result.delta(),
                    result.beforeStock(),
                    result.afterStock()
            );

            productStockHistoryRepository.save(history); // 재고이력 저장
        }

        var status = requestDto.status();
        if (status != null) {
            switch (status) {
                case ACTIVE -> {
                    if (product.getStatus() != ACTIVE)
                        product.active();
                }
                case INACTIVE -> {
                    if (product.getStatus() != INACTIVE)
                        product.inActive();
                }
            }
        }

        /** 도메인 이벤트 발행 (먼저)
         * - 이벤트가 있을 수도 있고 / 없을 수도 있고
         * - 어플리케이션은 도메인에 어떤 비즈니스 규칙이 있는지 모름
         */
        product.pullEvents().forEach(eventPublisher::publish);

        /** 어플리케이션 이벤트 발행 (변경된 경우에만)
         */
        if (!oldName.equals(product.getName()) || oldPrice != product.getPrice()) {
            eventPublisher.publish(new ProductReplicaUpdatedEvent(
                    LocalDateTime.now(),
                    product.getId(),
                    product.getName(),
                    product.getPrice()
            ));
        }

        return ProductUpdateResponseDto.from(product);
    }
}
