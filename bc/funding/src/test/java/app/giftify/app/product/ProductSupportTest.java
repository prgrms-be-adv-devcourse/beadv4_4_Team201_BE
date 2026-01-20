package app.giftify.app.product;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.domain.FundingMember;
import app.giftify.domain.product.Product;
import app.giftify.domain.product.exception.ProductException;
import app.giftify.out.product.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductSupportTest {

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductSupport productSupport;

	@Test
	@DisplayName("ID로 상품을 조회한다")
	void findById_returnsProduct() {
		// given
		Long productId = 1L;
		FundingMember seller = new FundingMember(1L, "test@test.com", "판매자", null, null, null, "홍길동", null, null);
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

		when(productRepository.findById(productId)).thenReturn(Optional.of(product));

		// when
		Product result = productSupport.findById(productId);

		// then
		assertThat(result.getName()).isEqualTo("테스트 상품");
		verify(productRepository).findById(productId);
	}

	@Test
	@DisplayName("존재하지 않는 ID로 조회하면 예외를 던진다")
	void findById_notFound_throwsException() {
		// given
		Long productId = 999L;

		when(productRepository.findById(productId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> productSupport.findById(productId))
			.isInstanceOf(ProductException.class);

		verify(productRepository).findById(productId);
	}

	@Test
	@DisplayName("상품 ID와 판매자 ID로 상품을 조회한다")
	void findByIdAndSellerId_returnsProduct() {
		// given
		Long productId = 1L;
		Long sellerId = 1L;
		FundingMember seller = new FundingMember(sellerId, "test@test.com", "판매자", null, null, null, "홍길동", null, null);
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);

		when(productRepository.findByIdAndSellerId(productId, sellerId)).thenReturn(Optional.of(product));

		// when
		Product result = productSupport.findByIdAndSellerId(productId, sellerId);

		// then
		assertThat(result.getName()).isEqualTo("테스트 상품");
		verify(productRepository).findByIdAndSellerId(productId, sellerId);
	}

	@Test
	@DisplayName("상품 ID와 판매자 ID로 조회 시 상품이 없으면 예외를 던진다")
	void findByIdAndSellerId_notFound_throwsException() {
		// given
		Long productId = 1L;
		Long sellerId = 999L;

		when(productRepository.findByIdAndSellerId(productId, sellerId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> productSupport.findByIdAndSellerId(productId, sellerId))
			.isInstanceOf(ProductException.class);

		verify(productRepository).findByIdAndSellerId(productId, sellerId);
	}
}