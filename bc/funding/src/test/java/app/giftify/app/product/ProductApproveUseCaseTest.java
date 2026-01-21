package app.giftify.app.product;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.domain.FundingMember;
import app.giftify.domain.product.Product;
import app.giftify.domain.product.ProductStatus;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.product.ProductReplicaCreationRequestedEvent;

@ExtendWith(MockitoExtension.class)
class ProductApproveUseCaseTest {

	@Mock
	private ProductSupport productSupport;

	@Mock
	private EventPublisher eventPublisher;

	@InjectMocks
	private ProductApproveUseCase productApproveUseCase;

	@Test
	@DisplayName("상품을 승인하면 INACTIVE 상태가 된다")
	void approveProduct_changesStatusToInactive() {
		// given
		Long productId = 1L;
		FundingMember seller = new FundingMember(1L, "auth0|123", "홍길동");
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

		when(productSupport.findById(productId)).thenReturn(product);

		// when
		productApproveUseCase.approveProduct(productId);

		// then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
		verify(productSupport).findById(productId);
	}

	@Test
	@DisplayName("상품을 승인하면 ProductVerifiedEvent가 발행된다")
	void approveProduct_publishesProductVerifiedEvent() {
		// given
		Long productId = 1L;
		FundingMember seller = new FundingMember(1L, "auth0|123", "홍길동");
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

		when(productSupport.findById(productId)).thenReturn(product);

		// when
		productApproveUseCase.approveProduct(productId);

		// then
		verify(eventPublisher).publish(any(ProductReplicaCreationRequestedEvent.class));
	}
}
