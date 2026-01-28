package app.giftify.product.application;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import app.giftify.product.domain.Product;
import app.giftify.product.adapter.inbound.MyProductSearchDto;
import app.giftify.product.adapter.inbound.ProductDto;
import app.giftify.product.adapter.inbound.ProductSearchDto;
import app.giftify.product.adapter.outbound.ProductRepository;
import app.giftify.shared.api.paging.PageResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductSearchUseCase {

	private final ProductRepository productRepository;

	public PageResponse<ProductDto> searchProducts(ProductSearchDto searchDto) {
		Page<Product> result = productRepository.searchProducts(searchDto);

		List<ProductDto> content = result.getContent().stream()
			.map(ProductDto::from)
			.toList();

		return PageResponse.of(
			content,
			searchDto.getPage(),
			searchDto.getSize(),
			result.getTotalElements()
		);
	}

	public PageResponse<ProductDto> searchMyProducts(Long sellerId, MyProductSearchDto searchDto) {
		Page<Product> result = productRepository.searchMyProducts(sellerId, searchDto);

		List<ProductDto> content = result.getContent().stream()
			.map(ProductDto::from)
			.toList();

		return PageResponse.of(
			content,
			searchDto.getPage(),
			searchDto.getSize(),
			result.getTotalElements()
		);
	}

}
