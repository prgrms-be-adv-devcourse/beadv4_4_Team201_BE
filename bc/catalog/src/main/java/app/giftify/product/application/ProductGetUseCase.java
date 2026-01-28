package app.giftify.product.application;

import static app.giftify.product.domain.ProductStatus.*;
import static app.giftify.product.domain.exception.ProductErrorCode.*;

import org.springframework.stereotype.Service;

import app.giftify.product.domain.Product;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.product.adapter.inbound.ProductDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductGetUseCase {
	private final ProductSupport productSupport;

	/**
	 * 상품 단건 조회
	 */
	public ProductDto getProduct(Long productId) {
		Product product = productSupport.findById(productId); // 상품이 없으면 404

		// 판매중이 아닌 상품
		if (!product.getStatus().equals(ACTIVE))
			throw new ProductException(PRODUCT_NOT_ACTIVE); // 400

		return ProductDto.from(product);
	}
}
