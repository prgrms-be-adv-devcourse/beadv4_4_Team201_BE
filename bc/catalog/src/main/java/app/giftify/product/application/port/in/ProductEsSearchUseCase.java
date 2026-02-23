package app.giftify.product.application.port.in;

import app.giftify.product.application.port.out.ProductEsSearchCommand;
import app.giftify.shared.api.paging.PageResponse;

public interface ProductEsSearchUseCase {
    PageResponse<ProductResult> searchByEs(ProductEsSearchCommand command);
}
