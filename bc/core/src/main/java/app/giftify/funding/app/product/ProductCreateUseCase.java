package app.giftify.funding.app.product;

import static app.giftify.funding.domain.product.exception.ProductErrorCode.*;

import org.springframework.stereotype.Service;

import app.giftify.funding.domain.FundingMember;
import app.giftify.funding.domain.product.Product;
import app.giftify.funding.domain.product.exception.ProductException;
import app.giftify.funding.in.product.ProductCreateRequestDto;
import app.giftify.funding.in.product.ProductDto;
import app.giftify.funding.out.FundingMemberRepository;
import app.giftify.funding.out.product.ProductRepository;
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
