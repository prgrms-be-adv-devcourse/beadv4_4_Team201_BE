package app.product;

import java.util.List;

import org.springframework.stereotype.Service;

import domain.product.Product;
import in.product.ProductDto;
import lombok.RequiredArgsConstructor;
import out.product.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductGetUseCase {
	private final ProductRepository productRepository;

	// 목록 조회
	public List<ProductDto> getProducts() {
		List<ProductDto> productList = productRepository.findAll().stream().map(Product::toDto).toList();

		return productList;
	}

	// 단건 조회
}
