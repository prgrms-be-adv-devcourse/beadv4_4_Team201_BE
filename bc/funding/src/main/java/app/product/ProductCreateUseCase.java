package app.product;

import org.springframework.stereotype.Service;

import domain.FundingMember;
import domain.product.Product;
import in.product.ProductCreateRequestDto;
import lombok.RequiredArgsConstructor;
import out.product.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductCreateUseCase {
	private final ProductRepository productRepository;

	public void createProduct(FundingMember seller, ProductCreateRequestDto requestDto) {
		Product product = new Product(
			seller, requestDto.name(), requestDto.description(), requestDto.price(), requestDto.stock()
		);
		productRepository.save(product);
	}
}
