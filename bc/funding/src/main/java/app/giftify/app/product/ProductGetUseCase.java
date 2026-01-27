package app.giftify.app.product;

import static app.giftify.domain.product.ProductStatus.*;
import static app.giftify.domain.product.exception.ProductErrorCode.*;

import org.springframework.stereotype.Service;

import app.giftify.domain.product.Product;
import app.giftify.domain.product.exception.ProductException;
import app.giftify.in.product.ProductDto;
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
