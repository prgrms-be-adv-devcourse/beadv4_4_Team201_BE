package app.giftify.app.product;

import static app.giftify.domain.product.exception.ProductErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Component;

import app.giftify.domain.product.Product;
import app.giftify.domain.product.exception.ProductException;
import app.giftify.out.product.ProductRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductSupport {
	private final ProductRepository productRepository;

	public Product findById(Long id) {
		return productRepository.findById(id).orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));
	}

	public Product findByIdAndSellerId(Long id, Long sellerId) {
		return productRepository.findByIdAndSellerId(id, sellerId)
			.orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));
	}

	public List<Product> findAllById(List<Long> productsIds) {
		return productRepository.findAllById(productsIds);
	}
}