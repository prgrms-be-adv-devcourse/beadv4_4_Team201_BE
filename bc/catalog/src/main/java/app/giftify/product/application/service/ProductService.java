package app.giftify.product.application.service;

import app.giftify.product.adapter.inbound.web.requestDto.MyProductSearchDto;
import app.giftify.product.adapter.inbound.web.requestDto.ProductSearchDto;
import app.giftify.product.adapter.inbound.web.requestDto.ProductUpdateRequestDto;
import app.giftify.product.application.port.in.*;
import app.giftify.product.application.port.out.*;
import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductPolicy;
import app.giftify.product.domain.event.ProductAcceptedEvent;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.replica.member.Member;
import app.giftify.replica.member.MemberRepository;
import app.giftify.shared.api.exception.InfraException;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.product.ProductDeletedEvent;
import app.giftify.shared.domain.event.product.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static app.giftify.product.domain.ProductStatus.ACTIVE;
import static app.giftify.product.domain.exception.ProductErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements ProductCreateUseCase, ProductGetUseCase, ProductSearchUseCase, ProductApproveUseCase, ProductRejectUseCase, ProductUpdateUseCase, DecreaseProductStockUseCase, ProductDeleteUseCase, HardDeleteExpiredProductUseCase {
    private final ProductRepositoryPort productRepositoryPort;
    private final ProductStockHistoryRepositoryPort productStockHistoryRepositoryPort;
    private final MemberRepository memberRepository;
    private final EventPublisher eventPublisher;
    private final ProductSupport productSupport;
    private final FundingClientPort fundingClientPort;

    // 상품 생성 (판매자)
    @Override
    @Transactional
    public ProductResult createProduct(Long sellerId, ProductCreateCommand command) {
        Member seller = memberRepository.findById(sellerId)
                .orElseThrow(() -> new ProductException(SELLER_NOT_FOUND));

        Product product = Product.builder()
                .sellerId(seller.getId())
                .name(command.name())
                .description(command.description())
                .price(command.price())
                .stock(command.stock())
                .category(command.category())
                .imageKey(command.imageKey())
                .build();

        Product savedProduct = productRepositoryPort.save(product);

        return ProductResult.of(savedProduct, seller.getNickname());
    }

    // 상품 단건 조회
    @Override
    @Transactional(readOnly = true)
    public ProductResult getProduct(Long productId) {
        Product product = productSupport.findById(productId);

        Member seller = memberRepository.findById(product.getSellerId())
                .orElseThrow(() -> new ProductException(SELLER_NOT_FOUND));

        if (!product.getStatus().equals(ACTIVE)) {
            throw new ProductException(PRODUCT_NOT_ACTIVE);
        }

        return ProductResult.of(product, seller.getNickname());
    }

    // 상품 검색
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResult> searchProducts(ProductSearchDto searchDto) {
        ProductSearchCommand command = new ProductSearchCommand(
                searchDto.getKeyword(),
                searchDto.getMinPrice(),
                searchDto.getMaxPrice(),
                searchDto.getInStock(),
                searchDto.getSort(),
                searchDto.getPage(),
                searchDto.getSize()
        );

        Page<Product> result = productRepositoryPort.searchProducts(command);
        List<ProductResult> content = toProductResults(result.getContent());

        return PageResponse.of(
                content,
                command.getPage(),
                command.getSize(),
                result.getTotalElements()
        );
    }

    // 내가 등록한 상품 검색 (판매자)
    @Override
    @Transactional(readOnly = true)
    public PageResponse<MyProductResult> searchMyProducts(Long sellerId, MyProductSearchDto searchDto) {
        MyProductSearchCommand command = new MyProductSearchCommand(
                searchDto.getKeyword(),
                searchDto.getMinPrice(),
                searchDto.getMaxPrice(),
                searchDto.getInStock(),
                searchDto.getSort(),
                searchDto.getPage(),
                searchDto.getSize(),
                searchDto.getStatus(),
                searchDto.isDeleted()
        );

        Page<Product> result = productRepositoryPort.searchMyProducts(sellerId, command);
        List<MyProductResult> content = toMyProductResults(result.getContent());

        return PageResponse.of(
                content,
                command.getPage(),
                command.getSize(),
                result.getTotalElements()
        );
    }

    // 상품 등록 승인 (관리자)
    @Override
    @Transactional
    public void approveProduct(Long productId) {
        Product product = productSupport.findById(productId);

        product.approve();
        productRepositoryPort.save(product);

        eventPublisher.publish(new ProductAcceptedEvent(productId)); // ES 도큐먼트 생성
    }

    // 상품 등록 거절 (관리자)
    @Override
    @Transactional
    public void rejectProduct(Long productId) {
        Product product = productSupport.findById(productId);

        product.reject();
        productRepositoryPort.save(product);
    }

    /**
     * 상품 수정 (판매자)
     * 재고 수정 시 비관적 락 + CAS 패턴 적용
     */
    @Override
    @Transactional
    public ProductUpdateResult updateProduct(Long productId, Long sellerId, ProductUpdateRequestDto requestDto) {
        Product product;

        if (requestDto.stock() != null) {
            if (requestDto.expectedStock() == null)
                throw new ProductException(EXPECTED_STOCK_REQUIRED);

            try { // 재고 수정 시 배타 잠금
                product = productSupport.findByIdForUpdate(productId);
            } catch (PessimisticLockingFailureException e) {
                throw new InfraException(PRODUCT_STOCK_LOCK_TIMEOUT);
            }
            validateProductOwner(product, sellerId);
            validateNotDeleted(product);

            if (product.getStock() != requestDto.expectedStock()) { // CAS 검증 (판매자가 재고 수정하려는 사이에 재고 변동이 일어남)
                throw new ProductException(PRODUCT_STOCK_CHANGED); // TODO 재시도 or 재고수정 분리
            }
            product.updateStock(requestDto.stock());
        } else {
            product = productSupport.findById(productId);
            validateProductOwner(product, sellerId);
            validateNotDeleted(product);
        }

        Optional.ofNullable(requestDto.name()).ifPresent(product::updateName);
        Optional.ofNullable(requestDto.description()).ifPresent(product::updateDescription);
        Optional.ofNullable(requestDto.price()).ifPresent(product::updatePrice);
        Optional.ofNullable(requestDto.imageKey()).ifPresent(product::updateImageKey);

        var status = requestDto.status();
        if (status != null) {
            switch (status) {
                case ACTIVE -> { // 도메인에서 상태 검증
                    product.active();
                }
                case INACTIVE -> { // 펀딩 체크
                    log.info("진행 중인 펀딩 확인 중 ...");
                    boolean isFundingOngoing = fundingClientPort.checkFundingExistsByProductId(productId);

                    if (isFundingOngoing) {
                        log.info("[판매 중지 변경 실패] 진행 중인 펀딩이 있습니다.");
                        throw new ProductException(CANNOT_STOP_SALE_DUE_TO_ACTIVE_FUNDING);
                    }
                    product.inActive();
                    log.info("[판매 중지 변경 성공] 변경 완료");
                }
            }
        }
        productRepositoryPort.save(product);
        product.pullEvents().forEach(eventPublisher::publish);
        eventPublisher.publish(new ProductUpdatedEvent(
                productId,
                product.getPrice(),
                product.getName(),
                product.getImageKey()
        ));
        log.info("상품 정보가 업데이트 되었습니다.");

        return ProductUpdateResult.from(product);
    }

    /**
     * 주문에 의한 재고 감소 (펀딩/일반 주문 통합)
     * 데드락 방지를 위해 productId 오름차순으로 배타 잠금 획득
     */
    @Transactional
    @Override
    public void decreaseStockByOrder(Map<Long, Integer> productQuantityMap) {
        var sorted = new TreeMap<>(productQuantityMap); // ProductId를 오름차순으로 정렬

        for (var entry : sorted.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();

            Product product;
            try {
                product = productSupport.findByIdForUpdate(productId);
            } catch (PessimisticLockingFailureException e) {
                throw new InfraException(PRODUCT_STOCK_LOCK_TIMEOUT);
            }

            product.decreaseStock(quantity);
            productRepositoryPort.saveAndFlush(product);

            product.pullEvents().forEach(eventPublisher::publish);
        }
    }

    // 도메인 -> ProductResult(애플리케이션 전용 dto/queryModel)
    // 컨트롤러에서 web Dto로 변환 (ProductDto)
    private List<ProductResult> toProductResults(List<Product> products) {
        Map<Long, String> sellerNicknameMap = getSellerNicknameMap(products);

        return products.stream()
                .map(product -> ProductResult.of(product, sellerNicknameMap.get(product.getSellerId())))
                .toList();
    }

    private List<MyProductResult> toMyProductResults(List<Product> products) {

        return products.stream()
                .map(MyProductResult::of)
                .toList();
    }

    private Map<Long, String> getSellerNicknameMap(List<Product> products) {
        List<Long> sellerIds = products.stream()
                .map(Product::getSellerId)
                .distinct()
                .toList();

        return memberRepository.findAllById(sellerIds).stream()
                .collect(Collectors.toMap(
                        Member::getId,
                        Member::getNickname
                ));
    }

    // 상품 판매자 검증
    private void validateProductOwner(Product product, Long sellerId) {
        if (!product.getSellerId().equals(sellerId))
            throw new ProductException(PRODUCT_NOT_OWNED);
    }

    // 삭제된 상품 검증
    private void validateNotDeleted(Product product) {
        if (product.getDeletedAt() != null)
            throw new ProductException(PRODUCT_ALREADY_DELETED);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId, Long sellerId) {
        Product product = productSupport.findByIdAndSellerId(productId, sellerId);
        product.delete();
        productRepositoryPort.save(product);

        eventPublisher.publish(new ProductDeletedEvent(productId));
    }

    @Override
    @Transactional
    public int hardDeleteExpiredProducts() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(ProductPolicy.DELETED_RETENTION_DAYS);

        List<Long> expiredProductIds = productRepositoryPort.findExpiredDeletedProductIds(cutoff);
        if (expiredProductIds.isEmpty()) {
            return 0;
        }

        // 삭제 순서: 재고 이력 -> 상품
        productStockHistoryRepositoryPort.deleteByProductIds(expiredProductIds);
        return productRepositoryPort.hardDeleteExpiredProducts(cutoff);
    }
}
