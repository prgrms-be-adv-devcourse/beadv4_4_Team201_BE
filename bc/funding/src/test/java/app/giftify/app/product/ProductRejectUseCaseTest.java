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
import app.giftify.domain.product.exception.ProductException;

@ExtendWith(MockitoExtension.class)
class ProductRejectUseCaseTest {

	@Mock
	private ProductSupport productSupport;

	@InjectMocks
	private ProductRejectUseCase productRejectUseCase;

	@Test
	@DisplayName("상품을 거절하면 REJECTED 상태가 된다")
	void rejectProduct_changesStatusToRejected() {
		// given
		Long productId = 1L;
		FundingMember seller = new FundingMember(1L, "test@test.com", "판매자", null, null, null, "홍길동", null, null);
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

		when(productSupport.findById(productId)).thenReturn(product);

		// when
		productRejectUseCase.rejectProduct(productId);

		// then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.REJECTED);
		verify(productSupport).findById(productId);
	}

	@Test
	@DisplayName("DRAFT 상태가 아닌 상품을 거절하면 예외가 발생한다")
	void rejectProduct_notDraftStatus_throwsException() {
		// given
		Long productId = 1L;
		FundingMember seller = new FundingMember(1L, "test@test.com", "판매자", null, null, null, "홍길동", null, null);
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);
		product.approve(); // INACTIVE 상태로 변경

		when(productSupport.findById(productId)).thenReturn(product);

		// when & then
		assertThatThrownBy(() -> productRejectUseCase.rejectProduct(productId))
			.isInstanceOf(ProductException.class);
	}
}