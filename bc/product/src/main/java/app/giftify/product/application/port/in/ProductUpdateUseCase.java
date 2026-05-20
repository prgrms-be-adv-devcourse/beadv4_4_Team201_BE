package app.giftify.product.application.port.in;

import app.giftify.product.adapter.inbound.web.requestDto.ProductUpdateRequestDto;

public interface ProductUpdateUseCase {
    ProductUpdateResult updateProduct(Long productId, Long sellerId, ProductUpdateRequestDto requestDto);
}

