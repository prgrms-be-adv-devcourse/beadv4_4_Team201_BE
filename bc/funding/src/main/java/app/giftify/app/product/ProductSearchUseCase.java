package app.giftify.app.product;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import app.giftify.domain.product.Product;
import app.giftify.in.product.ProductDto;
import app.giftify.in.product.ProductSearchDto;
import lombok.RequiredArgsConstructor;
import app.giftify.out.product.ProductQueryRepository;

@Service
@RequiredArgsConstructor
public class ProductSearchUseCase {
	private final ProductQueryRepository productQueryRepository;

	public Page<ProductDto> search(ProductSearchDto searchDto) {
		return productQueryRepository.search(searchDto).map(Product::toDto);
	}
}
