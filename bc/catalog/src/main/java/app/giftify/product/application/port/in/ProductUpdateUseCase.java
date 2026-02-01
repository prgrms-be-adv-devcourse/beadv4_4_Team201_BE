package app.giftify.product.application.port.in;

import app.giftify.product.adapter.inbound.web.requestDto.ProductUpdateRequestDto;
import app.giftify.product.adapter.inbound.web.responseDto.ProductUpdateResponseDto;

public interface ProductUpdateUseCase {
    ProductUpdateResponseDto updateProduct(Long productId, Long sellerId, ProductUpdateRequestDto requestDto);
}

