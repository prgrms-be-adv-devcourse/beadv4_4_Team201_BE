package app.giftify.app.product;

import static app.giftify.domain.product.exception.ProductErrorCode.*;

import org.springframework.stereotype.Service;

import app.giftify.domain.FundingMember;
import app.giftify.domain.product.Product;
import app.giftify.domain.product.exception.ProductException;
import app.giftify.in.product.ProductCreateRequestDto;
import app.giftify.in.product.ProductDto;
import app.giftify.out.FundingMemberRepository;
import app.giftify.out.product.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductCreateUseCase {
	private final ProductRepository productRepository;
	private final FundingMemberRepository fundingMemberRepository;

	public ProductDto createProduct(Long sellerId, ProductCreateRequestDto requestDto) {
		FundingMember seller = fundingMemberRepository.findById(sellerId)
			.orElseThrow(() -> new ProductException(FUNDING_MEMBER_NOT_FOUND));
		Product product = new Product(
			seller, requestDto.name(), requestDto.description(), requestDto.price(), requestDto.stock()
		);
		productRepository.save(product);

		return ProductDto.from(product);
	}
}
