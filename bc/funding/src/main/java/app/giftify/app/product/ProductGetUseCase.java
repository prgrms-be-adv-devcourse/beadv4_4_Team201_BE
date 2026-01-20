package app.giftify.app.product;

import org.springframework.stereotype.Service;

import app.giftify.in.product.ProductDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductGetUseCase {
	private final ProductSupport productSupport;

	public ProductDto getProduct(Long id) {
		return ProductDto.from(productSupport.findById(id));
	}
}
