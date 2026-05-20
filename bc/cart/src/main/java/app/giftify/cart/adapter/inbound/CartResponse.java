package app.giftify.cart.adapter.inbound;

import app.giftify.cart.core.domain.Cart;
import app.giftify.cart.core.domain.ItemStatus;

import java.util.List;

public record CartResponse(Long cartId, Long memberId, List<CartItemResponse> items, long totalAmount) {

    public static CartResponse from(Cart cart, List<CartItemResponse> itemResponses) {
        long total = itemResponses.stream()
                .filter(item -> item.status() == ItemStatus.AVAILABLE)
                .mapToLong(CartItemResponse::contributionAmount)
                .sum();

        return new CartResponse(cart.getId(), cart.getMemberId(), itemResponses, total);
    }
}
