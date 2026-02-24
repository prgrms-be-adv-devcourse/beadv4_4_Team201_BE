package app.giftify.funding.adpater.inbound;

import app.giftify.funding.application.SyncFundingProductUseCase;
import app.giftify.shared.domain.event.product.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductInfoChangeEventListener {

    private final SyncFundingProductUseCase syncFundingProductUseCase;

    @ApplicationModuleListener
    public void handle(ProductUpdatedEvent event) {
        syncFundingProductUseCase.syncFundingProduct(event.getProductId(), event.getProductPrice(), event.getProductName(), event.getImageKey());
    }
}