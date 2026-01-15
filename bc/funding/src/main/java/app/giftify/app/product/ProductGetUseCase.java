package app.giftify.app.product;

import java.util.List;

import org.springframework.stereotype.Service;

import app.giftify.domain.product.Product;
import app.giftify.in.product.ProductDto;

import app.giftify.out.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import app.giftify.out.product.ProductRepository;

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
