package app.giftify.product.application.port.in;

import app.giftify.product.adapter.inbound.web.requestDto.ProductEsSearchDto;
import app.giftify.shared.api.paging.PageResponse;

public interface ProductEsSearchUseCase {
    PageResponse<ProductResult> searchByEs(ProductEsSearchDto searchDto);
}
