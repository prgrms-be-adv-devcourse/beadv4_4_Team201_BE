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
        return dto;
    }
}
