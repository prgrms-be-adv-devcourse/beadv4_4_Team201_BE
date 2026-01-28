package app.giftify.funding.app.product;

import org.springframework.stereotype.Service;

import app.giftify.funding.domain.product.Product;
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
