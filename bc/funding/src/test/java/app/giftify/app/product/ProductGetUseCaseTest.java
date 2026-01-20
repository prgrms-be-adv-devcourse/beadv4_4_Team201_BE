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
import app.giftify.in.product.ProductDto;

@ExtendWith(MockitoExtension.class)
class ProductGetUseCaseTest {

	@Mock
	private ProductSupport productSupport;

	@InjectMocks
	private ProductGetUseCase productGetUseCase;

	@Test
	@DisplayName("상품 단건을 조회한다")
	void getProduct_returnsProductDto() {
		// given
		Long productId = 1L;
		FundingMember seller = new FundingMember(1L, "test@test.com", "판매자", null, null, null, "홍길동", null, null);
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

		when(productSupport.findById(productId)).thenReturn(product);

		// when
		ProductDto result = productGetUseCase.getProduct(productId);

		// then
		assertThat(result.name()).isEqualTo("테스트 상품");
		assertThat(result.description()).isEqualTo("테스트 설명");
		assertThat(result.price()).isEqualTo(10000);
		verify(productSupport).findById(productId);
	}
}