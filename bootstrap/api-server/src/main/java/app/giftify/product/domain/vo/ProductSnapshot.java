package app.giftify.product.domain.vo;

public record ProductSnapshot(
        Long productId,
        int price,
        Long sellerId,
        boolean purchasable
) {
}
