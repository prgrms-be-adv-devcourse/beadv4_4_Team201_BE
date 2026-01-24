package app.giftify.app.product;

import static app.giftify.domain.product.exception.ProductErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import app.giftify.domain.FundingMember;
import app.giftify.domain.product.Product;
import app.giftify.domain.product.ProductSnapshot;
import app.giftify.domain.product.exception.ProductException;
import app.giftify.in.product.ProductSnapshotDto;
import app.giftify.in.product.ProductSnapshotRequestDto;
import app.giftify.out.product.ProductSnapshotRepository;

@ExtendWith(MockitoExtension.class)
class ProductSnapshotCreateUseCaseTest {

	@Mock
	private ProductSupport productSupport;

	@Mock
	private ProductSnapshotRepository productSnapshotRepository;

	@InjectMocks
	private ProductSnapshotCreateUseCase productSnapshotCreateUseCase;

	@Test
	@DisplayName("상품 ID 목록으로 스냅샷을 생성한다")
	void createProductSnapshots_Success() {
		// given
		FundingMember seller = new FundingMember(1L, "auth0|123", "판매자");
		Product product1 = new Product(seller, "상품1", "설명1", 10000, 100);
		Product product2 = new Product(seller, "상품2", "설명2", 20000, 50);
		ReflectionTestUtils.setField(product1, "id", 1L);
		ReflectionTestUtils.setField(product2, "id", 2L);
		product1.approve();
		product1.active();
		product2.approve();
		product2.active();

		List<ProductSnapshotRequestDto.SnapshotItem> items = List.of(
			new ProductSnapshotRequestDto.SnapshotItem(1L),
			new ProductSnapshotRequestDto.SnapshotItem(2L)
		);
		ProductSnapshotRequestDto requestDto = new ProductSnapshotRequestDto(items);

		given(productSupport.findAllById(List.of(1L, 2L)))
			.willReturn(List.of(product1, product2));

		given(productSnapshotRepository.save(any(ProductSnapshot.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		// when
		List<ProductSnapshotDto> result = productSnapshotCreateUseCase.createProductSnapshots(requestDto);

		// then
		assertThat(result).hasSize(2);
		verify(productSnapshotRepository, times(2)).save(any(ProductSnapshot.class));
	}

	@Test
	@DisplayName("일부 상품을 찾을 수 없으면 예외가 발생한다")
	void createProductSnapshots_SomeProductsNotFound_ThrowsException() {
		// given
		FundingMember seller = new FundingMember(1L, "auth0|123", "판매자");
		Product product1 = new Product(seller, "상품1", "설명1", 10000, 100);
		ReflectionTestUtils.setField(product1, "id", 1L);

		List<ProductSnapshotRequestDto.SnapshotItem> items = List.of(
			new ProductSnapshotRequestDto.SnapshotItem(1L),
			new ProductSnapshotRequestDto.SnapshotItem(2L),
			new ProductSnapshotRequestDto.SnapshotItem(3L)
		);
		ProductSnapshotRequestDto requestDto = new ProductSnapshotRequestDto(items);

		// 3개 요청했는데 1개만 반환
		given(productSupport.findAllById(List.of(1L, 2L, 3L)))
			.willReturn(List.of(product1));

		// when & then
		assertThatThrownBy(() -> productSnapshotCreateUseCase.createProductSnapshots(requestDto))
			.isInstanceOf(ProductException.class)
			.satisfies(ex -> assertThat(((ProductException)ex).getErrorCode()).isEqualTo(PRODUCTS_NOT_FOUND));
	}

	@Test
	@DisplayName("단일 상품 스냅샷을 생성한다")
	void createProductSnapshots_SingleProduct_Success() {
		// given
		FundingMember seller = new FundingMember(1L, "auth0|123", "판매자닉네임");
		Product product = new Product(seller, "테스트 상품", "테스트 설명", 15000, 10);
		ReflectionTestUtils.setField(product, "id", 1L);
		product.approve();
		product.active();

		List<ProductSnapshotRequestDto.SnapshotItem> items = List.of(
			new ProductSnapshotRequestDto.SnapshotItem(1L)
		);
		ProductSnapshotRequestDto requestDto = new ProductSnapshotRequestDto(items);

		given(productSupport.findAllById(List.of(1L)))
			.willReturn(List.of(product));

		given(productSnapshotRepository.save(any(ProductSnapshot.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		// when
		List<ProductSnapshotDto> result = productSnapshotCreateUseCase.createProductSnapshots(requestDto);

		// then
		assertThat(result).hasSize(1);
		ProductSnapshotDto snapshotDto = result.get(0);
		assertThat(snapshotDto.name()).isEqualTo("테스트 상품");
		assertThat(snapshotDto.price()).isEqualTo(15000);
		assertThat(snapshotDto.sellerNickname()).isEqualTo("판매자닉네임");
		assertThat(snapshotDto.onSale()).isTrue();
	}

	@Test
	@DisplayName("빈 요청에 대해 빈 결과를 반환한다")
	void createProductSnapshots_EmptyRequest_ReturnsEmptyList() {
		// given
		ProductSnapshotRequestDto requestDto = new ProductSnapshotRequestDto(List.of());

		given(productSupport.findAllById(List.of()))
			.willReturn(List.of());

		// when
		List<ProductSnapshotDto> result = productSnapshotCreateUseCase.createProductSnapshots(requestDto);

		// then
		assertThat(result).isEmpty();
		verify(productSnapshotRepository, never()).save(any());
	}
}