package app.giftify.product.application.port.in;

import app.giftify.product.adapter.inbound.web.responseDto.ProductDto;
import app.giftify.product.application.port.out.ProductRepositoryPort;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.replica.member.Member;
import app.giftify.replica.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static app.giftify.product.domain.ProductStatus.ACTIVE;
import static app.giftify.product.domain.exception.ProductErrorCode.*;

@Service
@RequiredArgsConstructor
public class ProductGetUseCase {
    private final ProductRepositoryPort productRepositoryPort;
    private final MemberRepository memberRepository;

    /**
     * 상품 단건 조회
     */
    @Transactional(readOnly = true)
    public ProductDto getProduct(Long productId) {
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND)); // 상품이 없으면 404

        Member seller = memberRepository.findById(product.getSellerId())
                .orElseThrow(() -> new ProductException(SELLER_NOT_FOUND)); // 판매자가 없으면 404 (todo 판매자 탈퇴하면?)

        // 판매중이 아닌 상품
        if (!product.getStatus().equals(ACTIVE))
            throw new ProductException(PRODUCT_NOT_ACTIVE); // 400

        return ProductDto.from(product, seller.getNickname());
    }
}
