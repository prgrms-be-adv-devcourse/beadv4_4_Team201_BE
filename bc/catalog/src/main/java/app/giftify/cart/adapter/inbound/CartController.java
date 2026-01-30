package app.giftify.cart.adapter.inbound;

import app.giftify.cart.application.inbound.AddCartItemCommand;
import app.giftify.cart.application.inbound.CartService;
import app.giftify.cart.core.domain.CartItemKey;
import app.giftify.security.common.context.AuthenticatedMember;
import app.giftify.shared.domain.vo.Money;
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

    /**
     * 장바구니에 상품 추가
     */
    @PostMapping("/add")
    public ResponseEntity<Void> addItem(@AuthenticatedMember Long memberId, @RequestBody CartItemRequest request) {
        cartService.addItem(memberId, new AddCartItemCommand(
                new CartItemKey(request.targetType(), request.targetId()),
                Money.of(request.amount())
        ));
        return ResponseEntity.ok().build();
    }
}
