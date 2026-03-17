package app.giftify.product.adapter.outbound.jpa;

import app.giftify.product.adapter.inbound.web.requestDto.MyProductSearchDto;
import app.giftify.product.adapter.inbound.web.requestDto.ProductSearchDto;
import app.giftify.product.adapter.outbound.jpa.entity.ProductJpa;
import app.giftify.product.adapter.outbound.jpa.repository.ProductRepository;
import app.giftify.product.application.port.out.MyProductSearchCommand;
import app.giftify.product.application.port.out.ProductRepositoryPort;
import app.giftify.product.application.port.out.ProductSearchCommand;
import app.giftify.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductAdapter implements ProductRepositoryPort {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public Product save(Product product) {
        ProductJpa entity = productMapper.toEntity(product);
        ProductJpa savedEntity = productRepository.save(entity);
        return productMapper.toDomain(savedEntity);
    }

    @Override
    public void saveAndFlush(Product product) {
        ProductJpa entity = productMapper.toEntity(product);
        productRepository.saveAndFlush(entity);
    }

    @Override
    public Optional<Product> findById(Long productId) {
        return productRepository.findById(productId)
                .map(productMapper::toDomain);
    }

    @Override
    public Optional<Product> findByIdAndSellerId(Long productId, Long sellerId) {
        return productRepository.findByIdAndSellerId(productId, sellerId)
                .map(productMapper::toDomain);
    }

    /**
     * 일반 상품 검색(buyer), 판매자 상품 검색(seller)
     */
    @Override
    public Page<Product> searchProducts(ProductSearchCommand command) {
        ProductSearchDto dto = toDto(command);
        Page<ProductJpa> entityPage = productRepository.searchProducts(dto);
        return entityPage.map(productMapper::toDomain);
    }

    @Override
    public Page<Product> searchMyProducts(Long sellerId, MyProductSearchCommand command) {
        MyProductSearchDto dto = toDto(command);
        Page<ProductJpa> entityPage = productRepository.searchMyProducts(sellerId, dto);
        return entityPage.map(productMapper::toDomain);
    }

    @Override
    public List<Product> findAllById(List<Long> productsIds) {
        return productRepository.findAllById(productsIds).stream()
                .map(productMapper::toDomain)
                .toList();
    }

    /**
     * 재고 동시성 제어 : 비관적 락 배타 잠금
     * 락 범위를 재고 변경 시에만 적용할 수 있도록한 전용 데이터 조회 메서드
     */
    @Override
    public Optional<Product> findByIdForUpdate(Long productId) {
        return productRepository.findByIdForUpdate(productId)
                .map(productMapper::toDomain);
    }

    @Override
    public List<Long> findExpiredDeletedProductIds(LocalDateTime cutoff) {
        return productRepository.findExpiredDeletedProductIds(cutoff);
    }

    @Override
    public int hardDeleteExpiredProducts(LocalDateTime cutoff) {
        return productRepository.deleteExpiredProducts(cutoff);
    }

    private ProductSearchDto toDto(ProductSearchCommand command) {
        ProductSearchDto dto = new ProductSearchDto();
        dto.setKeyword(command.getKeyword());
        dto.setMinPrice(command.getMinPrice());
        dto.setMaxPrice(command.getMaxPrice());
        dto.setInStock(command.getInStock());
        dto.setSort(command.getSort());
        dto.setPage(command.getPage());
        dto.setSize(command.getSize());
        return dto;
    }

    private MyProductSearchDto toDto(MyProductSearchCommand command) {
        MyProductSearchDto dto = new MyProductSearchDto();
        dto.setKeyword(command.getKeyword());
        dto.setMinPrice(command.getMinPrice());
        dto.setMaxPrice(command.getMaxPrice());
        dto.setInStock(command.getInStock());
        dto.setSort(command.getSort());
        dto.setPage(command.getPage());
        dto.setSize(command.getSize());
        dto.setStatus(command.getStatus());
        dto.setDeleted(command.isDeleted());
        return dto;
    }
}
