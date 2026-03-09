package app.giftify.cart.adapter.inbound;

public record CartItemRequest(
        Long targetId,
        Long amount
) {
}
