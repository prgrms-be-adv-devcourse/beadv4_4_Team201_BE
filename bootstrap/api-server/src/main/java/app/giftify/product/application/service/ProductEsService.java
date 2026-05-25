package app.giftify.product.application.service;

import app.giftify.product.application.port.in.ProductEsSearchUseCase;
import app.giftify.product.application.port.in.ProductEsSyncUseCase;
import app.giftify.product.application.port.in.ProductResult;
import app.giftify.product.application.port.out.ProductEsPort;
import app.giftify.product.application.port.out.ProductEsSearchCommand;
import app.giftify.support.common.api.paging.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductEsService implements ProductEsSyncUseCase, ProductEsSearchUseCase {

    private final ProductEsPort productEsPort;

    @Override
    public int syncAll() {
        return productEsPort.syncAll();
    }

    @Override
    public PageResponse<ProductResult> searchByEs(ProductEsSearchCommand command) {
        return productEsPort.searchProducts(command);
    }
}
