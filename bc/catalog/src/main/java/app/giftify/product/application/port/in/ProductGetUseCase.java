package app.giftify.product.application.port.in;

import app.giftify.product.adapter.inbound.web.responseDto.ProductDto;
import app.giftify.product.adapter.outbound.jpa.entity.Product;
import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.replica.member.Member;
import app.giftify.replica.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static app.giftify.product.domain.ProductStatus.ACTIVE;
import static app.giftify.product.domain.exception.ProductErrorCode.PRODUCT_NOT_ACTIVE;
import static app.giftify.product.domain.exception.ProductErrorCode.SELLER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProductGetUseCase {
    private final ProductSupport productSupport;
    private final MemberRepository memberRepository;

    /**
     * 상품 단건 조회
     */
    public ProductDto getProduct(Long productId) {
        Product product = productSupport.findById(productId); // 상품이 없으면 404
        Member seller = memberRepository.findById(product.getSellerId())
                .orElseThrow(() -> new ProductException(SELLER_NOT_FOUND)); // 판매자가 없으면 404 (todo 판매자 탈퇴하면?)

        // 판매중이 아닌 상품
        if (!product.getStatus().equals(ACTIVE))
            throw new ProductException(PRODUCT_NOT_ACTIVE); // 400

        return ProductDto.from(product, seller.getNickname());
    }
}
