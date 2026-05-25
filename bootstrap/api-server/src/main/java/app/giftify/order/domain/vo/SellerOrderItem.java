package app.giftify.order.domain.vo;

public record SellerOrderItem(
        Long sellerId,
        Long productId,
        String productName,
        int quantity
) {
}