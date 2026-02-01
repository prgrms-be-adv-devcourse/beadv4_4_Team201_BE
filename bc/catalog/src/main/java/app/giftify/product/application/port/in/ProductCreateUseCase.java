package app.giftify.product.application.port.in;

import app.giftify.product.adapter.inbound.web.requestDto.ProductCreateRequestDto;
import app.giftify.product.adapter.inbound.web.responseDto.ProductDto;
import app.giftify.product.adapter.outbound.jpa.entity.Product;
import app.giftify.product.adapter.outbound.jpa.repository.ProductRepository;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.replica.member.Member;
import app.giftify.replica.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static app.giftify.product.domain.exception.ProductErrorCode.SELLER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProductCreateUseCase {
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    public ProductDto createProduct(Long sellerId, ProductCreateRequestDto requestDto) {
        Member seller = memberRepository.findById(sellerId)
                .orElseThrow(() -> new ProductException(SELLER_NOT_FOUND));

        Product product = Product.builder()
                .sellerId(seller.getId())
                .name(requestDto.name())
                .description(requestDto.description())
                .price(requestDto.price())
                .stock(requestDto.stock())
                .build();

        productRepository.save(product);
        return ProductDto.from(product, seller.getNickname());
    }
}
