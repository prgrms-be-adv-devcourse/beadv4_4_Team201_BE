package app.giftify.product.application.port.in;

import app.giftify.product.adapter.inbound.web.requestDto.ProductUpdateRequestDto;
import app.giftify.product.adapter.inbound.web.responseDto.ProductUpdateResponseDto;
import app.giftify.product.adapter.outbound.jpa.entity.ProductStockHistory;
import app.giftify.product.adapter.outbound.jpa.repository.ProductStockHistoryRepository;
import app.giftify.product.application.port.out.ProductRepositoryPort;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.product.ProductReplicaUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static app.giftify.product.domain.ProductStatus.ACTIVE;
import static app.giftify.product.domain.ProductStatus.INACTIVE;
import static app.giftify.product.domain.exception.ProductErrorCode.PRODUCT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProductUpdateUseCase {
    private final ProductRepositoryPort productRepositoryPort;
    private final ProductStockHistoryRepository productStockHistoryRepository;
    private final EventPublisher eventPublisher;

    // todo @Lock
    @Transactional
    public ProductUpdateResponseDto updateProduct(Long productId, Long sellerId, ProductUpdateRequestDto requestDto) {
        Product product = productRepositoryPort.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));

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

            ProductStockHistory history = ProductStockHistory.manualAdjust(
                    product.getSellerId(),
                    product.getId(),
                    result.delta(),
                    result.beforeStock(),
                    result.afterStock()
            );
            productStockHistoryRepository.save(history); // 재고 이력 저장
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

        productRepositoryPort.save(product);

        /**
         * 도메인 이벤트 발행 (먼저)
         * - 이벤트가 있을 수도 있고 / 없을 수도 있고
         * - 어플리케이션은 도메인에 어떤 비즈니스 규칙이 있는지 모름
         */
        product.pullEvents().forEach(eventPublisher::publish);

        /**
         * 어플리케이션 이벤트 발행 (변경된 경우에만)
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

