package app.giftify.cart.adapter.inbound;

public record CartItemRequest(
        Long wishlistId,
        Long wishlistItemId,
        Long amount
) {
}
