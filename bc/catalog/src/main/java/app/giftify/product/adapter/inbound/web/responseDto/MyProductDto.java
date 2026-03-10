package app.giftify.product.adapter.inbound.web.responseDto;

import app.giftify.product.adapter.outbound.jpa.entity.ProductJpa;
import app.giftify.product.application.port.in.MyProductResult;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;

import java.time.LocalDateTime;

public record MyProductDto(
        Long id,
        String name,
        String description,
        int price,
        int stock,
        ProductStatus status,
        String imageKey,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
    public static MyProductDto from(ProductJpa productJpa) {
        if (productJpa == null)
            return null;

        return new MyProductDto(
                productJpa.getId(),
                productJpa.getName(),
                productJpa.getDescription(),
                productJpa.getPrice(),
                productJpa.getStock(),
                productJpa.getStatus(),
                productJpa.getImageKey(),
                productJpa.getCreatedAt(),
                productJpa.getUpdatedAt(),
                productJpa.getDeletedAt()
        );
    }

    public static MyProductDto from(Product product) {
        if (product == null)
            return null;

        return new MyProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getStatus(),
                product.getImageKey(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getDeletedAt()
        );
    }

    public static MyProductDto from(MyProductResult result) {
        if (result == null)
            return null;

        return new MyProductDto(
                result.id(),
                result.name(),
                result.description(),
                result.price(),
                result.stock(),
                result.status(),
                result.imageKey(),
                result.createdAt(),
                result.updatedAt(),
                result.deletedAt()
        );
    }
}
