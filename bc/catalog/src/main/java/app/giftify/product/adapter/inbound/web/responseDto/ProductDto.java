package app.giftify.product.adapter.inbound.web.responseDto;

import java.time.LocalDateTime;

import app.giftify.product.adapter.outbound.jpa.entity.ProductJpa;
import app.giftify.product.application.port.in.ProductResult;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductCategory;

public record ProductDto(
        Long id,
        String sellerNickName,
        String name,
        String description,
        int price,
        ProductCategory category,
        String imageKey,
        LocalDateTime createdAt
) {
    public static ProductDto from(ProductJpa productJpa, String sellerNickname) {
        if (productJpa == null)
            return null;

        return new ProductDto(
                productJpa.getId(),
                sellerNickname,
                productJpa.getName(),
                productJpa.getDescription(),
                productJpa.getPrice(),
                productJpa.getCategory(),
                productJpa.getImageKey(),
                productJpa.getCreatedAt()
        );
    }

    public static ProductDto from(Product product, String sellerNickname) {
        if (product == null)
            return null;

        return new ProductDto(
                product.getId(),
                sellerNickname,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getImageKey(),
                product.getCreatedAt()
        );
    }

    public static ProductDto from(ProductResult result) {
        if (result == null)
            return null;

        return new ProductDto(
                result.id(),
                result.sellerNickName(),
                result.name(),
                result.description(),
                result.price(),
                result.category(),
                result.imageKey(),
                result.createdAt()
        );
    }
}
