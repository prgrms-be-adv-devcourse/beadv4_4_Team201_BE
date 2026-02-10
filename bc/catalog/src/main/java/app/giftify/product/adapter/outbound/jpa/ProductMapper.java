package app.giftify.product.adapter.outbound.jpa;

import org.springframework.stereotype.Component;

import app.giftify.product.adapter.outbound.jpa.entity.ProductJpa;
import app.giftify.product.domain.Product;

@Component
public class ProductMapper {

    public Product toDomain(ProductJpa entity) {
        if (entity == null) {
            return null;
        }
        return Product.builder()
                .id(entity.getId())
                .sellerId(entity.getSellerId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .stock(entity.getStock())
                .status(entity.getStatus())
                .category(entity.getCategory())
                .imageKey(entity.getImageKey())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ProductJpa toEntity(Product domain) {
        if (domain == null) {
            return null;
        }
        // 기존 엔티티 수정 (ID 있음)
        if (domain.getId() != null) {
            return new ProductJpa(
                    domain.getId(),
                    domain.getSellerId(),
                    domain.getName(),
                    domain.getDescription(),
                    domain.getPrice(),
                    domain.getStock(),
                    domain.getStatus(),
                    domain.getCategory(),
                    domain.getImageKey()
            );
        }
        // 새 엔티티 생성 (ID 없음)
        return ProductJpa.builder()
                .sellerId(domain.getSellerId())
                .name(domain.getName())
                .description(domain.getDescription())
                .price(domain.getPrice())
                .stock(domain.getStock())
                .status(domain.getStatus())
                .category(domain.getCategory())
                .imageKey(domain.getImageKey())
                .build();
    }
}
