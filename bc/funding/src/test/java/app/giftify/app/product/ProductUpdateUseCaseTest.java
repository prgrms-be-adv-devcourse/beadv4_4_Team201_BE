package app.giftify.app.product;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.domain.FundingMember;
import app.giftify.domain.product.Product;
import app.giftify.domain.product.ProductStatus;
import app.giftify.in.product.ProductUpdateRequestDto;
import app.giftify.in.product.ProductUpdateResponseDto;
import app.giftify.out.product.ProductStockHistoryRepository;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.product.ProductSnapshotUpdatedEvent;

@ExtendWith(MockitoExtension.class)
class ProductUpdateUseCaseTest {

	@Mock
	private ProductSupport productSupport;

	@Mock
	private EventPublisher eventPublisher;

	@Mock
	private ProductStockHistoryRepository productStockHistoryRepository;

	@InjectMocks
	private ProductUpdateUseCase productUpdateUseCase;

	private FundingMember seller;
	private Long productId;
	private Long sellerId;

	@BeforeEach
	void setUp() {
		productId = 1L;
		sellerId = 1L;
		seller = new FundingMember(1L, "auth0|123", "홍길동");
	}

	@Nested
	@DisplayName("상품 정보 수정")
	class UpdateProductInfo {

		@Test
		@DisplayName("상품 이름을 수정할 수 있다")
		void updateProduct_changesName() {
			// given
			Product product = new Product(seller, "원래 상품명", "테스트 설명", 10000, 100);
			product.approve();
			ProductUpdateRequestDto requestDto = new ProductUpdateRequestDto("변경된 상품명", "ㅇㄹ", null, null, null);

			when(productSupport.findByIdAndSellerId(productId, sellerId)).thenReturn(product);

			// when
			ProductUpdateResponseDto result = productUpdateUseCase.updateProduct(productId, sellerId, requestDto);

			// then
			assertThat(result.name()).isEqualTo("변경된 상품명");
		}

		@Test
		@DisplayName("상품 설명을 수정할 수 있다")
		void updateProduct_changesDescription() {
			// given
			Product product = new Product(seller, "테스트 상품", "원래 설명", 10000, 100);
			product.approve();
			ProductUpdateRequestDto requestDto = new ProductUpdateRequestDto(null, "변경된 설명", null, null, null);

			when(productSupport.findByIdAndSellerId(productId, sellerId)).thenReturn(product);

			// when
			ProductUpdateResponseDto result = productUpdateUseCase.updateProduct(productId, sellerId, requestDto);

			// then
			assertThat(result.description()).isEqualTo("변경된 설명");
		}

		@Test
		@DisplayName("상품 가격을 수정할 수 있다")
		void updateProduct_changesPrice() {
			// given
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);
			product.approve();
			ProductUpdateRequestDto requestDto = new ProductUpdateRequestDto(null, null, 20000, null, null);

			when(productSupport.findByIdAndSellerId(productId, sellerId)).thenReturn(product);

			// when
			ProductUpdateResponseDto result = productUpdateUseCase.updateProduct(productId, sellerId, requestDto);

			// then
			assertThat(result.price()).isEqualTo(20000);
		}

		@Test
		@DisplayName("상품 재고를 수정할 수 있다")
		void updateProduct_changesStock() {
			// given
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);
			product.approve();
			ProductUpdateRequestDto requestDto = new ProductUpdateRequestDto(null, null, null, 50, null);

			when(productSupport.findByIdAndSellerId(productId, sellerId)).thenReturn(product);

			// when
			ProductUpdateResponseDto result = productUpdateUseCase.updateProduct(productId, sellerId, requestDto);

			// then
			assertThat(result.stock()).isEqualTo(50);
		}
	}

	@Nested
	@DisplayName("상품 상태 변경")
	class UpdateProductStatus {

		@Test
		@DisplayName("INACTIVE 상품을 ACTIVE로 변경할 수 있다")
		void updateProduct_activatesProduct() {
			// given
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);
			product.approve(); // INACTIVE 상태
			ProductUpdateRequestDto requestDto = new ProductUpdateRequestDto(null, null, null, null,
				ProductStatus.ACTIVE);

			when(productSupport.findByIdAndSellerId(productId, sellerId)).thenReturn(product);

			// when
			ProductUpdateResponseDto result = productUpdateUseCase.updateProduct(productId, sellerId, requestDto);

			// then
			assertThat(result.status()).isEqualTo(ProductStatus.ACTIVE);
		}

		@Test
		@DisplayName("ACTIVE 상품을 INACTIVE로 변경할 수 있다")
		void updateProduct_deactivatesProduct() {
			// given
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);
			product.approve();
			product.active(); // ACTIVE 상태
			ProductUpdateRequestDto requestDto = new ProductUpdateRequestDto(null, null, null, null,
				ProductStatus.INACTIVE);

			when(productSupport.findByIdAndSellerId(productId, sellerId)).thenReturn(product);

			// when
			ProductUpdateResponseDto result = productUpdateUseCase.updateProduct(productId, sellerId, requestDto);

			// then
			assertThat(result.status()).isEqualTo(ProductStatus.INACTIVE);
		}
	}

	@Nested
	@DisplayName("이벤트 발행")
	class EventPublishing {

		@Test
		@DisplayName("상품 수정 시 ProductModifiedEvent가 발행된다")
		void updateProduct_publishesProductModifiedEvent() {
			// given
			Product product = new Product(seller, "테스트 상품", "테스트 설명", 10000, 100);
			product.approve();
			ProductUpdateRequestDto requestDto = new ProductUpdateRequestDto("변경된 상품명", null, null, null, null);

			when(productSupport.findByIdAndSellerId(productId, sellerId)).thenReturn(product);

			// when
			productUpdateUseCase.updateProduct(productId, sellerId, requestDto);

			// then
			verify(eventPublisher).publish(any(ProductSnapshotUpdatedEvent.class));
		}
	}
}