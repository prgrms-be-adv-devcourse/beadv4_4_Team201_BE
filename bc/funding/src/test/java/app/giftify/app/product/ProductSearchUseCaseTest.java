package app.giftify.app.product;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import app.giftify.domain.FundingMember;
import app.giftify.domain.product.Product;
import app.giftify.domain.product.ProductStatus;
import app.giftify.in.product.MyProductSearchDto;
import app.giftify.in.product.ProductDto;
import app.giftify.in.product.ProductSearchDto;
import app.giftify.out.product.ProductRepository;
import app.giftify.shared.api.paging.PageResponse;

@ExtendWith(MockitoExtension.class)
class ProductSearchUseCaseTest {

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductSearchUseCase productSearchUseCase;

	@Test
	@DisplayName("일반 검색 결과는 ACTIVE 상태인 상품만 반환한다")
	void searchProducts_returnsOnlyActiveProducts() {
		// given
		ProductSearchDto searchDto = new ProductSearchDto();
		searchDto.setPage(0);
		searchDto.setSize(10);

		FundingMember seller = new FundingMember(1L, "test@test.com", "판매자", null, null, null, "홍길동", null, null);

		// 여러 상태의 상품 생성
		Product draftProduct = new Product(seller, "등록대기 상품", "설명", 10000, 50);
		assertThat(draftProduct.getStatus()).isEqualTo(ProductStatus.DRAFT);

		Product inactiveProduct = new Product(seller, "비활성 상품", "설명", 20000, 30);
		inactiveProduct.approve();
		assertThat(inactiveProduct.getStatus()).isEqualTo(ProductStatus.INACTIVE);

		Product rejectedProduct = new Product(seller, "거절된 상품", "설명", 15000, 40);
		rejectedProduct.reject();
		assertThat(rejectedProduct.getStatus()).isEqualTo(ProductStatus.REJECTED);

		Product activeProduct1 = createActiveProduct(seller, "활성 상품1", 30000, 100);
		Product activeProduct2 = createActiveProduct(seller, "활성 상품2", 40000, 200);

		// Repository는 ACTIVE 상태 상품만 반환
		Page<Product> page = new PageImpl<>(List.of(activeProduct1, activeProduct2));
		when(productRepository.searchProducts(searchDto)).thenReturn(page);

		// when
		PageResponse<ProductDto> result = productSearchUseCase.searchProducts(searchDto);

		// then
		assertThat(result.content()).hasSize(2);
		assertThat(result.content()).extracting(ProductDto::name)
			.containsExactly("활성 상품1", "활성 상품2");
		assertThat(result.totalElements()).isEqualTo(2);
		verify(productRepository).searchProducts(searchDto);
	}

	@Test
	@DisplayName("키워드와 가격 필터로 상품을 검색한다")
	void searchProducts_withKeywordAndPriceFilter_returnsMatchingProducts() {
		// given
		ProductSearchDto searchDto = new ProductSearchDto();
		searchDto.setKeyword("노트북");
		searchDto.setMinPrice(500000);
		searchDto.setMaxPrice(1500000);
		searchDto.setPage(0);
		searchDto.setSize(10);

		FundingMember seller = new FundingMember(1L, "test@test.com", "판매자", null, null, null, "홍길동", null, null);
		Product product1 = createActiveProduct(seller, "삼성 노트북", 1000000, 10);
		Product product2 = createActiveProduct(seller, "LG 노트북", 1200000, 5);

		Page<Product> page = new PageImpl<>(List.of(product1, product2));
		when(productRepository.searchProducts(searchDto)).thenReturn(page);

		// when
		PageResponse<ProductDto> result = productSearchUseCase.searchProducts(searchDto);

		// then
		assertThat(result.content()).hasSize(2);
		assertThat(result.content()).extracting(ProductDto::name)
			.containsExactly("삼성 노트북", "LG 노트북");
		assertThat(result.pageNumber()).isEqualTo(0);
		assertThat(result.pageSize()).isEqualTo(10);
		assertThat(result.totalElements()).isEqualTo(2);
		verify(productRepository).searchProducts(searchDto);
	}

	@Test
	@DisplayName("내가 판매하는 상품 조회 - 기본적으로는 모든 상태의 상품을 반환한다")
	void searchMyProducts_noStatusFilter_returnsAllMyProducts() {
		// given
		Long sellerId = 1L;
		MyProductSearchDto searchDto = new MyProductSearchDto();
		searchDto.setPage(0);
		searchDto.setSize(10);

		FundingMember seller = new FundingMember(sellerId, "test@test.com", "판매자", null, null, null, "홍길동", null, null);

		// 모든 상태의 상품 생성
		Product draftProduct = new Product(seller, "등록대기 상품", "설명", 10000, 100);
		assertThat(draftProduct.getStatus()).isEqualTo(ProductStatus.DRAFT);

		Product inactiveProduct = new Product(seller, "비활성 상품", "설명", 20000, 50);
		inactiveProduct.approve();
		assertThat(inactiveProduct.getStatus()).isEqualTo(ProductStatus.INACTIVE);

		Product activeProduct = createActiveProduct(seller, "활성 상품", 30000, 200);

		Product rejectedProduct = new Product(seller, "거절된 상품", "설명", 15000, 30);
		rejectedProduct.reject();
		assertThat(rejectedProduct.getStatus()).isEqualTo(ProductStatus.REJECTED);

		// 상태 상관없이 모든 상품 반환
		Page<Product> page = new PageImpl<>(List.of(draftProduct, inactiveProduct, activeProduct, rejectedProduct));
		when(productRepository.searchMyProducts(sellerId, searchDto)).thenReturn(page);

		// when
		PageResponse<ProductDto> result = productSearchUseCase.searchMyProducts(sellerId, searchDto);

		// then
		assertThat(result.content()).hasSize(4);
		assertThat(result.content()).extracting(ProductDto::name)
			.containsExactly("등록대기 상품", "비활성 상품", "활성 상품", "거절된 상품");
		assertThat(result.totalElements()).isEqualTo(4);
		verify(productRepository).searchMyProducts(sellerId, searchDto);
	}

	@Test
	@DisplayName("내가 판매하는 상품 조회 - DRAFT 상태로 필터링하면 DRAFT 상품만 반환한다")
	void searchMyProducts_filterByDraft_returnsDraftProducts() {
		// given
		Long sellerId = 1L;
		MyProductSearchDto searchDto = new MyProductSearchDto();
		searchDto.setPage(0);
		searchDto.setSize(10);
		searchDto.setStatus(ProductStatus.DRAFT);

		FundingMember seller = new FundingMember(sellerId, "test@test.com", "판매자", null, null, null, "홍길동", null, null);
		Product draftProduct = new Product(seller, "등록대기 상품", "설명", 10000, 100);
		assertThat(draftProduct.getStatus()).isEqualTo(ProductStatus.DRAFT);

		Page<Product> page = new PageImpl<>(List.of(draftProduct));
		when(productRepository.searchMyProducts(sellerId, searchDto)).thenReturn(page);

		// when
		PageResponse<ProductDto> result = productSearchUseCase.searchMyProducts(sellerId, searchDto);

		// then
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).name()).isEqualTo("등록대기 상품");
		verify(productRepository).searchMyProducts(sellerId, searchDto);
	}

	private Product createActiveProduct(FundingMember seller, String name, int price, int stock) {
		Product product = new Product(seller, name, "설명", price, stock);
		product.approve();
		product.active();
		assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
		return product;
	}
}
