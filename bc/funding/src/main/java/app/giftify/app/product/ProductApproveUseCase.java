package app.giftify.app.product;

import org.springframework.stereotype.Service;

import app.giftify.domain.product.Product;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductApproveUseCase {
	private final ProductSupport productSupport;

	public void approveProduct(Long id) {
		Product product = productSupport.findById(id);
		product.approve();
	}
}
