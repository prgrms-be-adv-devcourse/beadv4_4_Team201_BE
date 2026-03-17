package app.giftify.shared.domain.vo;

public record ProductSnapshot(
        Long productId,
        int price,
        Long sellerId
) {
}
