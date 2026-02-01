package app.giftify.product.application.port.in;

import app.giftify.product.application.port.out.ProductRepositoryPort;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.product.ProductReplicaCreationRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static app.giftify.product.domain.exception.ProductErrorCode.PRODUCT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProductApproveUseCase {
    private final ProductRepositoryPort productRepositoryPort;
    private final EventPublisher eventPublisher;

    @Transactional
    public void approveProduct(Long id) {
        Product product = productRepositoryPort.findById(id)
                .orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));

        product.approve();
        productRepositoryPort.save(product);

        // 4. 애플리케이션 이벤트 발행
        // 상품 승인되어 ProductReplicaCreationRequestedEvent 발행 (sync)
        eventPublisher.publish(
                new ProductReplicaCreationRequestedEvent(
                        LocalDateTime.now(),
                        product.getId(),
                        product.getName(),
                        product.getPrice()
                )
        );
    }
}
