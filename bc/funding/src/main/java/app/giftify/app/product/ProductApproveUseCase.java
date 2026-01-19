package app.giftify.app.product;

import org.springframework.stereotype.Service;

import app.giftify.domain.product.Product;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.product.ProductModifiedEvent;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductApproveUseCase {
	private final ProductSupport productSupport;
	private final EventPublisher eventPublisher;

	public void approveProduct(Long id) {
		Product product = productSupport.findById(id);
		product.approve();

		/**
		 * todo 멤버모듈상품 sync Event
		 */
		eventPublisher.publish(new ProductModifiedEvent(product.toSnapshot()));
	}
}
