package app.giftify.app.product;

import static app.giftify.in.product.web.ProductErrorCode.*;

import org.springframework.stereotype.Component;

import app.giftify.domain.product.Product;
import app.giftify.in.product.web.ProductException;
import app.giftify.out.product.ProductRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductSupport {
	private final ProductRepository productRepository;

	public Product findById(Long id) {
		return productRepository.findById(id).orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));
	}
}