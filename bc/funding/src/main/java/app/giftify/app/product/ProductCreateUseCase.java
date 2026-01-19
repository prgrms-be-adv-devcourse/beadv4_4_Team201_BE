package app.giftify.app.product;

import org.springframework.stereotype.Service;

import app.giftify.domain.FundingMember;
import app.giftify.domain.product.Product;
import app.giftify.in.product.ProductCreateRequestDto;
import app.giftify.in.product.ProductDto;
import app.giftify.out.product.ProductRepository;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.product.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductCreateUseCase {
	private final ProductRepository productRepository;
	private final EventPublisher eventPublisher;

	public ProductDto createProduct(FundingMember seller, ProductCreateRequestDto requestDto) {
		Product product = new Product(
			seller, requestDto.name(), requestDto.description(), requestDto.price(), requestDto.stock()
		);
		productRepository.save(product);

		/**
		 * todo 멤버모듈상품 sync Event
		 */
		eventPublisher.publish(new ProductCreatedEvent(product.toSnapshot()));

		return product.toDto();
	}
}
