package app.giftify.shared.domain.vo;

public record SellerOrderItem(
        Long sellerId,
        Long productId,
        String productName,
        int quantity
) {
}