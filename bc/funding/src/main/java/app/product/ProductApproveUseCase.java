package app.product;

import org.springframework.stereotype.Service;

import domain.product.Product;
import lombok.RequiredArgsConstructor;
import out.product.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductApproveUseCase {
	private final ProductRepository productRepository;

	public void approveProduct(Long id) {
		Product product = productRepository.findById(id).get();
		product.approveProduct();
	}
}
