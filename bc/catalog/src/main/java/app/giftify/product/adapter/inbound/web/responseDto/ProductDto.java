package app.giftify.product.adapter.inbound.web.responseDto;

import app.giftify.product.adapter.outbound.jpa.entity.Product;

import java.time.LocalDateTime;

public record ProductDto(
        Long id,
        String sellerNickName,
        String name,
        String description,
        int price,
        LocalDateTime createdAt
) {
    public static ProductDto from(Product product, String sellerNickname) {
        if (product == null)
            return null;

        return new ProductDto(
                product.getId(),
                sellerNickname,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCreatedAt()
        );
    }
}
