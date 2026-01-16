package app.giftify.app.product;

import org.springframework.stereotype.Service;

import app.giftify.domain.product.Product;

import app.giftify.out.product.ProductRepository;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ProductApproveUseCase {
	private final ProductRepository productRepository;

	public void approveProduct(Long id) {
		Product product = productRepository.findById(id).get();
		product.approveProduct();
	}
}
