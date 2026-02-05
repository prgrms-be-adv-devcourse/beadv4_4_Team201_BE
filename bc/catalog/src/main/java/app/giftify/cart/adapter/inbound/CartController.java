package app.giftify.cart.adapter.inbound;

import app.giftify.cart.application.inbound.AddCartItemCommand;
import app.giftify.cart.application.inbound.CartService;
import app.giftify.cart.core.domain.CartItemKey;
import app.giftify.security.common.context.AuthenticatedMember;
import app.giftify.shared.api.response.RsData;
import app.giftify.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/carts")
@RequiredArgsConstructor
public class CartController implements CartV2ApiSpec {
    private final CartService cartService;

    @Override
    @PostMapping("/{cartId}/add")
    public ResponseEntity<RsData<Void>> addItem(@PathVariable Long cartId, @RequestBody CartItemRequest request) {
        cartService.addItem(cartId, new AddCartItemCommand(
                new CartItemKey(request.targetType(), request.targetId()),
                Money.of(request.amount())
        ));
        return ResponseEntity.ok(RsData.success(null));
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<RsData<CartResponse>> getCart(@PathVariable Long cartId, @AuthenticatedMember Long memberId) {
        CartResponse response = cartService.getCart(cartId, memberId);
        return ResponseEntity.ok(RsData.success(response));
    }
}
