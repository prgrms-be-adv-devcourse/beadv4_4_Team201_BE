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
@RequestMapping("/api/v2/carts")
@RequiredArgsConstructor
public class CartController implements CartV2ApiSpec {
    private final CartService cartService;

    @Override
    @PostMapping("/add")
    public ResponseEntity<Void> addItem(@AuthenticatedMember Long memberId, @RequestBody CartItemRequest request) {
        cartService.addItem(memberId, new AddCartItemCommand(
                new CartItemKey(request.targetType(), request.targetId()),
                Money.of(request.amount())
        ));
        return ResponseEntity.ok().build();
    }
}
