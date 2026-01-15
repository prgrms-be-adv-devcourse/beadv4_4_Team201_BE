package app.product;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import domain.product.Product;
import in.product.ProductDto;
import in.product.ProductSearchDto;
import lombok.RequiredArgsConstructor;
import out.product.ProductQueryRepository;

@Service
@RequiredArgsConstructor
public class ProductSearchUseCase {
	private final ProductQueryRepository productQueryRepository;

	public Page<ProductDto> search(ProductSearchDto searchDto) {
		return productQueryRepository.search(searchDto).map(Product::toDto);
	}
}
