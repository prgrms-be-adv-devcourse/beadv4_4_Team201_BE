package app.giftify.cart.adapter.inbound;

import app.giftify.cart.application.inbound.CartService;
import app.giftify.security.common.context.AuthenticatedMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/add-item")
    public ResponseEntity<CartResponse> addItem(@AuthenticatedMember Long memberId, @RequestBody CartItemRequest request) {
        CartResponse response = cartService.addItem(memberId, request);
        return ResponseEntity.ok(response);
    }
}
