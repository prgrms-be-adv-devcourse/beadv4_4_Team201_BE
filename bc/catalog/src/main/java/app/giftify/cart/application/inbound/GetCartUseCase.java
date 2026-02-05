package app.giftify.cart.application.inbound;

import app.giftify.cart.adapter.inbound.CartResponse;

public interface GetCartUseCase {
    CartResponse getCart(Long cartId, Long memberId);

}
