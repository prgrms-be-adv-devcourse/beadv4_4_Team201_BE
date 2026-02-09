package app.giftify.product.domain;

public record StockChangeResult(
        Long sellerId,
        Long productId,
        int beforeStock,
        int afterStock,
        int delta,
        StockChangeType changeType
) {
}
