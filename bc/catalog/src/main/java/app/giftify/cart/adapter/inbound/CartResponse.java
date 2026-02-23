package app.giftify.cart.adapter.inbound;

import app.giftify.cart.core.domain.Cart;
import app.giftify.cart.core.domain.CartItem;
import app.giftify.product.domain.Product;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record CartResponse(Long cartId, Long memberId, List<CartItemResponse> items, long totalAmount) {

    public static CartResponse from(Cart cart, List<CartItem> validItems, Map<Long, Product> productMap) {
        List<CartItemResponse> itemResponses = validItems.stream()
                .map(item -> CartItemResponse.from(item, productMap.get(item.getTargetId())))
                .collect(Collectors.toList());

        long total = validItems.stream()
                .mapToLong(item -> item.getAmount().amount().longValue())
                .sum();

        return new CartResponse(
                cart.getId(),
                cart.getMemberId(),
                itemResponses,
                total
        );
    }
}
