package app.giftify.wishlist.adapter.out.api.dto;

public record ProductResponse(
        Long id,
        String name,
        int price,
        String status,
        String sellerNickName
) {
}
