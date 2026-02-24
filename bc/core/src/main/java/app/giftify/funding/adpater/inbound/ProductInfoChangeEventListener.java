package app.giftify.funding.adpater.inbound;

import app.giftify.funding.application.SyncFundingProductUseCase;
import app.giftify.shared.domain.event.product.ProductPriceUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductInfoChangeEventListener {

    private final SyncFundingProductUseCase syncFundingProductUseCase;

    @ApplicationModuleListener
    public void handle(ProductPriceUpdatedEvent event) {
        syncFundingProductUseCase.syncFundingProduct(event.getProductId(), event.getProductPrice(), event.getProductName(), event.getImageKey());
    }
}