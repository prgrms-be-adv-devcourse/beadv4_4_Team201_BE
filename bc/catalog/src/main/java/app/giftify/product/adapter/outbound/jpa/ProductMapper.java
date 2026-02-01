package app.giftify.product.adapter.outbound.jpa;

import app.giftify.product.adapter.outbound.jpa.entity.ProductJpa;
import app.giftify.product.domain.Product;
import org.springframework.stereotype.Component;

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
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ProductJpa toEntity(Product domain) {
        if (domain == null) {
            return null;
        }
        return ProductJpa.builder()
                // id is auto-generated, not set here
                .sellerId(domain.getSellerId())
                .name(domain.getName())
                .description(domain.getDescription())
                .price(domain.getPrice())
                .stock(domain.getStock())
                .status(domain.getStatus())
                .build();
    }
}
