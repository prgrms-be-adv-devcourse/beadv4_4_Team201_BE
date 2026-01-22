package app.giftify.app.product;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import app.giftify.in.product.ProductCreateRequestDto;
import app.giftify.in.product.ProductDto;
import app.giftify.out.FundingMemberRepository;
import app.giftify.out.product.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductCreateUseCaseTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private FundingMemberRepository fundingMemberRepository;

	@InjectMocks
	private ProductCreateUseCase productCreateUseCase;

	@Test
	@DisplayName("상품을 생성하면 ProductDto를 반환한다")
	void createProduct_returnsProductDto() {
		// given
		FundingMember seller = new FundingMember(1L, "auth0|123", "홍길동");
		ProductCreateRequestDto requestDto = new ProductCreateRequestDto("테스트 상품", "테스트 설명", 10000, 100);

		when(fundingMemberRepository.findById(1L)).thenReturn(Optional.of(seller));
		when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// when
		ProductDto result = productCreateUseCase.createProduct(seller.getId(), requestDto);

		// then
		assertThat(result.name()).isEqualTo("테스트 상품");
		assertThat(result.description()).isEqualTo("테스트 설명");
		assertThat(result.price()).isEqualTo(10000);
		assertThat(result.sellerNickName()).isEqualTo("홍길동");

		verify(productRepository).save(any(Product.class));
	}

	@Test
	@DisplayName("상품 생성 시 상품명과 설명의 앞/뒤 공백이 제거된다")
	void createProduct_trimsNameAndDescription() {
		// given
		FundingMember seller = new FundingMember(1L, "auth0|123", "홍길동");
		ProductCreateRequestDto requestDto = new ProductCreateRequestDto("  테스트 상품  ", "  테스트 설명  ", 10000, 100);

		when(fundingMemberRepository.findById(1L)).thenReturn(Optional.of(seller));
		when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// when
		ProductDto result = productCreateUseCase.createProduct(seller.getId(), requestDto);

		// then
		assertThat(result.name()).isEqualTo("테스트 상품");
		assertThat(result.description()).isEqualTo("테스트 설명");
	}
}
