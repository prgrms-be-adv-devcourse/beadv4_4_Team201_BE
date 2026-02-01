package app.giftify.product.application.service;

import app.giftify.product.adapter.inbound.web.requestDto.MyProductSearchDto;
import app.giftify.product.adapter.inbound.web.requestDto.ProductSearchDto;
import app.giftify.product.adapter.inbound.web.requestDto.ProductUpdateRequestDto;
import app.giftify.product.adapter.inbound.web.responseDto.ProductUpdateResponseDto;
import app.giftify.product.adapter.outbound.jpa.entity.ProductStockHistory;
import app.giftify.product.adapter.outbound.jpa.repository.ProductStockHistoryRepository;
import app.giftify.product.application.port.in.*;
import app.giftify.product.application.port.out.MyProductSearchCommand;
import app.giftify.product.application.port.out.ProductRepositoryPort;
import app.giftify.product.application.port.out.ProductSearchCommand;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.replica.member.Member;
import app.giftify.replica.member.MemberRepository;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.product.ProductReplicaCreationRequestedEvent;
import app.giftify.shared.domain.event.product.ProductReplicaUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static app.giftify.product.domain.ProductStatus.ACTIVE;
import static app.giftify.product.domain.ProductStatus.INACTIVE;
import static app.giftify.product.domain.exception.ProductErrorCode.*;

@Service
@RequiredArgsConstructor
public class ProductService implements ProductCreateUseCase, ProductGetUseCase, ProductSearchUseCase, ProductApproveUseCase, ProductRejectUseCase, ProductUpdateUseCase {
    private final ProductRepositoryPort productRepositoryPort;
    private final MemberRepository memberRepository;
    private final EventPublisher eventPublisher;
    private final ProductStockHistoryRepository productStockHistoryRepository;

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
                .build();

        Product savedProduct = productRepositoryPort.save(product);

        return ProductResult.of(savedProduct, seller.getNickname());
    }

    // 상품 단건 조회
    @Override
    @Transactional(readOnly = true)
    public ProductResult getProduct(Long productId) {
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));

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
    public PageResponse<ProductResult> searchMyProducts(Long sellerId, MyProductSearchDto searchDto) {
        MyProductSearchCommand command = new MyProductSearchCommand(
                searchDto.getKeyword(),
                searchDto.getMinPrice(),
                searchDto.getMaxPrice(),
                searchDto.getInStock(),
                searchDto.getSort(),
                searchDto.getPage(),
                searchDto.getSize(),
                searchDto.getStatus()
        );

        Page<Product> result = productRepositoryPort.searchMyProducts(sellerId, command);
        List<ProductResult> content = toProductResults(result.getContent());

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
    public void approveProduct(Long id) {
        Product product = productRepositoryPort.findById(id)
                .orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));

        product.approve();
        productRepositoryPort.save(product);

        eventPublisher.publish(
                new ProductReplicaCreationRequestedEvent(
                        LocalDateTime.now(),
                        product.getId(),
                        product.getName(),
                        product.getPrice()
                )
        );
    }

    // 상품 등록 거절 (관리자)
    @Override
    @Transactional
    public void rejectProduct(Long id) {
        Product product = productRepositoryPort.findById(id)
                .orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));

        product.reject();
        productRepositoryPort.save(product);
    }

    // 상품 수정 (판매자)
    @Override
    @Transactional
    public ProductUpdateResponseDto updateProduct(Long productId, Long sellerId, ProductUpdateRequestDto requestDto) {
        Product product = productRepositoryPort.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));

        String oldName = product.getName();
        int oldPrice = product.getPrice();

        Optional.ofNullable(requestDto.name()).ifPresent(product::updateName);
        Optional.ofNullable(requestDto.description()).ifPresent(product::updateDescription);
        Optional.ofNullable(requestDto.price()).ifPresent(product::updatePrice);

        Integer newStock = requestDto.stock();
        if (newStock != null && product.getStock() != newStock) {
            Product.StockChangeResult result = product.updateStock(newStock);

            ProductStockHistory history = ProductStockHistory.manualAdjust(
                    product.getSellerId(),
                    product.getId(),
                    result.delta(),
                    result.beforeStock(),
                    result.afterStock()
            );
            productStockHistoryRepository.save(history);
        }

        var status = requestDto.status();
        if (status != null) {
            switch (status) {
                case ACTIVE -> {
                    if (product.getStatus() != ACTIVE)
                        product.active();
                }
                case INACTIVE -> {
                    if (product.getStatus() != INACTIVE)
                        product.inActive();
                }
            }
        }

        productRepositoryPort.save(product);
        product.pullEvents().forEach(eventPublisher::publish);

        if (!oldName.equals(product.getName()) || oldPrice != product.getPrice()) {
            eventPublisher.publish(new ProductReplicaUpdatedEvent(
                    LocalDateTime.now(),
                    product.getId(),
                    product.getName(),
                    product.getPrice()
            ));
        }

        return ProductUpdateResponseDto.from(product);
    }

    // 도메인 -> ProductResult(애플리케이션 전용 dto/queryModel)
    // 컨트롤러에서 web Dto로 변환 (ProductDto)
    private List<ProductResult> toProductResults(List<Product> products) {
        Map<Long, String> sellerNicknameMap = getSellerNicknameMap(products);

        return products.stream()
                .map(product -> ProductResult.of(product, sellerNicknameMap.get(product.getSellerId())))
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
}
