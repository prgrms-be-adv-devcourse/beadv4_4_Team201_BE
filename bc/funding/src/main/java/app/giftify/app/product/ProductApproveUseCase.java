package app.giftify.app.product;

import org.springframework.stereotype.Service;

import app.giftify.domain.product.Product;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.product.ProductVerifiedEvent;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductApproveUseCase {
	private final ProductSupport productSupport;
	private final EventPublisher eventPublisher;

	public void approveProduct(Long id) {
		Product product = productSupport.findById(id);
		product.approve();

		// 상품 승인되어 ProductVerifiedEvent 발행 (sync)
		eventPublisher.publish(
			new ProductVerifiedEvent(
				product.getId(),
				product.getName(),
				product.getDescription(),
				product.getPrice(),
				product.getSeller().getNickname()
			)
		);
	}
}
