package app.giftify.product.application;

import app.giftify.catalogmember.adapter.outbound.jpa.CatalogMemberRepository;
import app.giftify.catalogmember.core.domain.CatalogMember;
import app.giftify.product.adapter.inbound.ProductCreateRequestDto;
import app.giftify.product.adapter.inbound.ProductDto;
import app.giftify.product.adapter.outbound.ProductRepository;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.exception.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static app.giftify.product.domain.exception.ProductErrorCode.SELLER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProductCreateUseCase {
    private final ProductRepository productRepository;
    private final CatalogMemberRepository catalogMemberRepository;

    public ProductDto createProduct(Long sellerId, ProductCreateRequestDto requestDto) {
        CatalogMember seller = catalogMemberRepository.findById(sellerId)
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
