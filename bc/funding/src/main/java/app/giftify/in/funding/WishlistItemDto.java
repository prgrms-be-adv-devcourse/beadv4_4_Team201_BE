package app.giftify.in.funding;

public record WishlistItemDto(
    Long wishlistItemId,
    Long fundingReceiverId,  // 위시리스트 소유자(펀딩 수령자) ID
    Long productId,
    String productName,
    Integer productPrice
) {
}

