package app.giftify.app.product;

import static app.giftify.domain.product.ProductStatus.*;

import java.util.Optional;

import org.springframework.stereotype.Service;

import app.giftify.domain.product.Product;
import app.giftify.in.product.ProductUpdateRequestDto;
import app.giftify.in.product.ProductUpdateResponseDto;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.product.ProductModifiedEvent;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductUpdateUseCase {
	private final ProductSupport productSupport;
	private final EventPublisher eventPublisher;

	// todo @Lock
	public ProductUpdateResponseDto updateProduct(Long productId, Long sellerId, ProductUpdateRequestDto requestDto) {
		Product product = productSupport.findByIdAndSellerId(productId, sellerId);

		// todo 더티체킹해서 변경사항 있을 때만 ProductModifiedEvent 발행하기?
		Optional.ofNullable(requestDto.name()).ifPresent(product::updateName); // 이름 수정
		Optional.ofNullable(requestDto.description()).ifPresent(product::updateDescription); // 설명 수정
		Optional.ofNullable(requestDto.price()).ifPresent(product::updatePrice); // 설명 수정
		Optional.ofNullable(requestDto.stock()).ifPresent(product::updateStock); // 재고 수정

		var status = requestDto.status();
		if (status != null) {
			switch (status) {
				case ACTIVE -> {
					if (product.getStatus() != ACTIVE)
						product.active();
				}
				case INACTIVE -> {
					if (product.getStatus() != INACTIVE)
						product.inActive();
				}
			}
		}

		/** 도메인 이벤트 발행 (먼저)
		 * - 이벤트가 있을 수도 있고 / 없을 수도 있고
		 * - 어플리케이션은 도메인에 어떤 비즈니스 규칙이 있는지 모름
		 */
		product.pullEvents().forEach(eventPublisher::publish);

		/** 어플리케이션 이벤트 발행
		 * todo 멤버모듈상품 sync Event
		 */
		eventPublisher.publish(new ProductModifiedEvent(
			product.getId(),
			product.getName(),
			product.getDescription(),
			product.getPrice(),
			product.getSeller().getNickname()
		));

		return ProductUpdateResponseDto.from(product);
	}
}