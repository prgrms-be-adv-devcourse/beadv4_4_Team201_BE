package app.giftify.product.application.port.in;

import app.giftify.product.adapter.outbound.jpa.entity.Product;
import app.giftify.product.application.support.ProductSupport;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.product.ProductReplicaCreationRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductApproveUseCase {
    private final ProductSupport productSupport;
    private final EventPublisher eventPublisher;

    public void approveProduct(Long id) {
        Product product = productSupport.findById(id);
        product.approve();

        // 상품 승인되어 ProductVerifiedEvent 발행 (sync)
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
