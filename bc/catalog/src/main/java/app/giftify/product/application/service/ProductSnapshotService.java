package app.giftify.product.application.service;

import app.giftify.product.adapter.inbound.web.requestDto.ProductSnapshotRequestDto;
import app.giftify.product.adapter.inbound.web.responseDto.ProductSnapshotDto;
import app.giftify.product.adapter.outbound.jpa.entity.ProductJpa;
import app.giftify.product.adapter.outbound.jpa.entity.ProductSnapshot;
import app.giftify.product.adapter.outbound.jpa.repository.ProductSnapshotRepository;
import app.giftify.product.application.port.in.ProductSnapshotCreateUseCase;
import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.replica.member.Member;
import app.giftify.replica.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static app.giftify.product.domain.exception.ProductErrorCode.PRODUCTS_NOT_FOUND;
import static app.giftify.product.domain.exception.ProductErrorCode.SELLER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductSnapshotService implements ProductSnapshotCreateUseCase {

    private final ProductSupport productSupport;
    private final ProductSnapshotRepository productSnapshotRepository;
    private final MemberRepository memberRepository;

    @Override
    public List<ProductSnapshotDto> createProductSnapshots(ProductSnapshotRequestDto requestDto) {
        List<Long> productIds = requestDto.items().stream()
                .map(ProductSnapshotRequestDto.SnapshotItem::productId)
                .toList();

        List<ProductJpa> productJpas = productSupport.findAllById(productIds);

        if (productJpas.size() != productIds.size()) {
            throw new ProductException(PRODUCTS_NOT_FOUND);
        }

        // 요청 순서 보장을 위해 Map으로 변환
        Map<Long, ProductJpa> productMap = productJpas.stream()
                .collect(Collectors.toMap(ProductJpa::getId, Function.identity()));

        // 판매자 정보 조회
        Set<Long> sellerIds = productJpas.stream()
                .map(ProductJpa::getSellerId)
                .collect(Collectors.toSet());
        Map<Long, Member> sellerMap = memberRepository.findAllById(sellerIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        // 요청된 productIds 순서대로 스냅샷 생성
        return productIds.stream()
                .map(productId -> {
                    ProductJpa productJpa = productMap.get(productId);
                    Member seller = sellerMap.get(productJpa.getSellerId());
                    if (seller == null) {
                        throw new ProductException(SELLER_NOT_FOUND);
                    }
                    ProductSnapshot snapshot = productSnapshotRepository.save(ProductSnapshot.from(productJpa, seller));
                    return ProductSnapshotDto.from(snapshot);
                }).toList();
    }
}
