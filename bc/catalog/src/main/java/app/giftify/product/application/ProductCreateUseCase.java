package app.giftify.product.application;

import static app.giftify.product.domain.exception.ProductErrorCode.*;

import org.springframework.stereotype.Service;

// TODO: Remove FundingMember dependency
// import app.giftify.domain.FundingMember;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.product.adapter.inbound.ProductCreateRequestDto;
import app.giftify.product.adapter.inbound.ProductDto;
// TODO: Remove FundingMember dependency
// import app.giftify.out.FundingMemberRepository;
import app.giftify.product.adapter.outbound.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductCreateUseCase {
	private final ProductRepository productRepository;
	// TODO: Remove FundingMember dependency
	// private final FundingMemberRepository fundingMemberRepository;

	public ProductDto createProduct(Long sellerId, ProductCreateRequestDto requestDto) {
		// TODO: Remove FundingMember dependency
		// FundingMember seller = fundingMemberRepository.findById(sellerId)
		// 	.orElseThrow(() -> new ProductException(FUNDING_MEMBER_NOT_FOUND));
		// Product product = new Product(
		// 	seller, requestDto.name(), requestDto.description(), requestDto.price(), requestDto.stock()
		// );
		// productRepository.save(product);
		//
		// return ProductDto.from(product);
		throw new UnsupportedOperationException("FundingMember dependency needs to be removed");
	}
}
