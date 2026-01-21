package app.giftify.wishlist.application.port.out;

public interface WishlistProductQueryPort {
    // bc:funding 모듈 API 호출용
    ProductStatus getProductStatus(Long productId);

    public record ProductStatus(
            Long productId,
            boolean onSale,
            String name,
            int price,
            String sellerNickName
    ) {
    }
}
