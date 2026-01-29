package app.giftify.product.adapter.inbound;

import app.giftify.catalogmember.core.domain.CatalogMember;
import app.giftify.product.domain.Product;

import java.time.LocalDateTime;

public record ProductDto(
        Long id,
        String sellerNickName,
        String name,
        String description,
        int price,
        LocalDateTime createdAt
) {
    public static ProductDto from(Product product, CatalogMember seller) {
        if (product == null)
            return null;

        return new ProductDto(
                product.getId(),
                seller.getNickname(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCreatedAt()
        );
    }
}
