package app.giftify.product.application.port.in;

import app.giftify.product.adapter.inbound.web.requestDto.MyProductSearchDto;
import app.giftify.product.adapter.inbound.web.requestDto.ProductSearchDto;
import app.giftify.shared.api.paging.PageResponse;

public interface ProductSearchUseCase {
    // 일반 상품 검색
    PageResponse<ProductResult> searchProducts(ProductSearchDto searchDto);

    // 내 상품 검색 (판매자)
    PageResponse<ProductResult> searchMyProducts(Long sellerId, MyProductSearchDto searchDto);
}
