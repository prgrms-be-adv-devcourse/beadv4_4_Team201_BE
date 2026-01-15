package app.giftify.app.product;

import org.springframework.stereotype.Service;

import app.giftify.domain.FundingMember;
import app.giftify.domain.product.Product;
import app.giftify.in.product.ProductCreateRequestDto;
import lombok.RequiredArgsConstructor;
import app.giftify.out.product.ProductRepository;

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
