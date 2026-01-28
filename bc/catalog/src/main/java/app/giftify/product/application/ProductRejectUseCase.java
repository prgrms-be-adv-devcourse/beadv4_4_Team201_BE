package app.giftify.product.application;

import org.springframework.stereotype.Service;

import app.giftify.product.domain.Product;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductRejectUseCase {
	private final ProductSupport productSupport;

	public void rejectProduct(Long id) {
		Product product = productSupport.findById(id);
		product.reject();
	}
}
