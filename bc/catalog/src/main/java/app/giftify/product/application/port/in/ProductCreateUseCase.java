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

import static app.giftify.product.domain.exception.ProductErrorCode.SELLER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProductCreateUseCase {
    private final ProductRepositoryPort productRepositoryPort;
    private final MemberRepository memberRepository;

    @Transactional
    public ProductDto createProduct(Long sellerId, ProductCreateCommand command) { // DTO -> Command
        Member seller = memberRepository.findById(sellerId)
                .orElseThrow(() -> new ProductException(SELLER_NOT_FOUND));

        Product product = Product.builder()
                .sellerId(seller.getId())
                .name(command.name()) // requestDto -> command
                .description(command.description()) // requestDto -> command
                .price(command.price()) // requestDto -> command
                .stock(command.stock()) // requestDto -> command
                .build();

        Product savedProduct = productRepositoryPort.save(product);
        return ProductDto.from(savedProduct, seller.getNickname());
    }
}
