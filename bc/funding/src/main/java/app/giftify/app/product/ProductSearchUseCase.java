package app.giftify.app.product;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


import app.giftify.domain.product.Product;
import app.giftify.in.product.ProductDto;
import app.giftify.in.product.ProductSearchDto;
import app.giftify.out.product.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductSearchUseCase {

	private final ProductRepository productRepository;

	public Page<ProductDto> search(ProductSearchDto searchDto) {
		return productRepository.search(searchDto).map(Product::toDto);
	}

}
