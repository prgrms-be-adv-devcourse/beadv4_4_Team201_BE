package app.giftify.product.application;

import app.giftify.product.adapter.inbound.ProductSnapshotDto;
import app.giftify.product.adapter.inbound.ProductSnapshotRequestDto;
import app.giftify.product.adapter.outbound.ProductSnapshotRepository;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductSnapshot;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.replica.member.Member;
import app.giftify.replica.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static app.giftify.product.domain.exception.ProductErrorCode.PRODUCTS_NOT_FOUND;
import static app.giftify.product.domain.exception.ProductErrorCode.SELLER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProductSnapshotCreateUseCase {
    private final ProductSupport productSupport;
    private final ProductSnapshotRepository productSnapshotRepository;
    private final MemberRepository memberRepository;

    /**
     * 장바구니에 담긴 상품들을 결제할 때, 상품들을 스냅샷으로 저장하여 전달
     */
    public List<ProductSnapshotDto> createProductSnapshots(ProductSnapshotRequestDto requestDto) {
        List<Long> productIds = requestDto.items().stream()
                .map(ProductSnapshotRequestDto.SnapshotItem::productId)
                .toList();

        List<Product> products = productSupport.findAllById(productIds);

        if (products.size() != productIds.size()) {
            throw new ProductException(PRODUCTS_NOT_FOUND);
        }

        // 요청 순서 보장을 위해 Map으로 변환
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 판매자 정보 조회
        Set<Long> sellerIds = products.stream()
                .map(Product::getSellerId)
                .collect(Collectors.toSet());
        Map<Long, Member> sellerMap = memberRepository.findAllById(sellerIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        // 요청된 productIds 순서대로 스냅샷 생성
        return productIds.stream()
                .map(productId -> {
                    Product product = productMap.get(productId);
                    Member seller = sellerMap.get(product.getSellerId());
                    if (seller == null) {
                        throw new ProductException(SELLER_NOT_FOUND);
                    }
                    ProductSnapshot snapshot = productSnapshotRepository.save(ProductSnapshot.from(product, seller));
                    return ProductSnapshotDto.from(snapshot);
                }).toList();
    }
}
