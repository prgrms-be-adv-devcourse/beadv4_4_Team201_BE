package app.giftify.app.product;

import static app.giftify.domain.product.exception.ProductErrorCode.*;
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
import app.giftify.domain.product.exception.ProductException;
import app.giftify.in.product.ProductDto;

@ExtendWith(MockitoExtension.class)
class ProductGetUseCaseTest {

	@Mock
	private ProductSupport productSupport;

	@InjectMocks
	private ProductGetUseCase productGetUseCase;

	@Test
	@DisplayName("ACTIVE 상품을 비회원이 조회한다")
	void getProduct_activeProduct_withoutLogin() {
		// given
		Long productId = 1L;
		FundingMember seller = new FundingMember(1L, "auth0|123", "홍길동");
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);
		product.approve();
		product.active();

		when(productSupport.findById(productId)).thenReturn(product);

		// when
		ProductDto result = productGetUseCase.getProduct(productId, null);

		// then
		assertThat(result.name()).isEqualTo("테스트 상품");
		assertThat(result.description()).isEqualTo("테스트 설명");
		assertThat(result.price()).isEqualTo(10000);
		assertThat(result.sellerNickName()).isEqualTo("홍길동");
		verify(productSupport).findById(productId);
	}

	@Test
	@DisplayName("비ACTIVE 상품을 판매자 본인이 조회한다")
	void getProduct_inactiveProduct_bySeller() {
		// given
		Long productId = 1L;
		Long sellerId = 1L;
		FundingMember seller = new FundingMember(sellerId, "auth0|123", "홍길동");
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

		when(productSupport.findById(productId)).thenReturn(product);

		// when
		ProductDto result = productGetUseCase.getProduct(productId, sellerId);

		// then
		assertThat(result.name()).isEqualTo("테스트 상품");
		verify(productSupport).findById(productId);
	}

	@Test
	@DisplayName("비ACTIVE 상품을 비회원이 조회하면 예외가 발생한다")
	void getProduct_inactiveProduct_withoutLogin_throwsException() {
		// given
		Long productId = 1L;
		FundingMember seller = new FundingMember(1L, "auth0|123", "홍길동");
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

		when(productSupport.findById(productId)).thenReturn(product);

		// when & then
		assertThatThrownBy(() -> productGetUseCase.getProduct(productId, null))
			.isInstanceOf(ProductException.class)
			.satisfies(ex -> assertThat(((ProductException)ex).getErrorCode()).isEqualTo(PRODUCT_NOT_ACTIVE));
	}

	@Test
	@DisplayName("비ACTIVE 상품을 판매자가 아닌 회원이 조회하면 예외가 발생한다")
	void getProduct_inactiveProduct_byOtherMember_throwsException() {
		// given
		Long productId = 1L;
		Long sellerId = 1L;
		Long otherMemberId = 999L;
		FundingMember seller = new FundingMember(sellerId, "auth0|123", "홍길동");
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

		when(productSupport.findById(productId)).thenReturn(product);

		// when & then
		assertThatThrownBy(() -> productGetUseCase.getProduct(productId, otherMemberId))
			.isInstanceOf(ProductException.class)
			.satisfies(ex -> assertThat(((ProductException)ex).getErrorCode()).isEqualTo(PRODUCT_NOT_ACTIVE));
	}
}