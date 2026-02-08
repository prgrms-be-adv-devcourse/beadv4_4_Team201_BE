package app.giftify.cart.adapter.inbound;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.cart.application.inbound.AddCartItemCommand;
import app.giftify.cart.application.inbound.CartService;
import app.giftify.cart.core.domain.CartItemAddResult;
import app.giftify.cart.core.domain.CartItemKey;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.RsData;
import app.giftify.shared.domain.vo.Money;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v2/carts")
@RequiredArgsConstructor
public class CartController implements CartV2ApiSpec {
	private final CartService cartService;

	@Override
	@PostMapping("/{cartId}")
	public ResponseEntity<RsData<Void>> addItem(@PathVariable("cartId") Long cartId,
		@RequestBody CartItemRequest request) {
		CartItemAddResult result = cartService.addItem(cartId, new AddCartItemCommand(
			new CartItemKey(request.targetType(), request.targetId()),
			Money.of(request.amount())
		));

		if (result == CartItemAddResult.UPDATED) {
			return ResponseEntity.ok(RsData.success(null, "이미 장바구니에 있는 펀딩으로 가격이 수정되었습니다."));
		}
		return ResponseEntity.ok(RsData.success(null, "펀딩이 장바구니에 담겼습니다."));
	}



	@Override
	@PostMapping
	public ResponseEntity<RsData<Void>> addItemToMyCart(
		@CurrentMemberId Long memberId,
		@RequestBody CartItemRequest request
	) {
		CartItemAddResult result = cartService.addItemToMyCart(memberId, new AddCartItemCommand(
			new CartItemKey(request.targetType(), request.targetId()),
			Money.of(request.amount())
		));

		if (result == CartItemAddResult.UPDATED) {
			return ResponseEntity.ok(RsData.success(null, "이미 장바구니에 있는 펀딩으로 가격이 수정되었습니다."));
		}
		return ResponseEntity.ok(RsData.success(null, "펀딩이 장바구니에 담겼습니다."));
	}

	@Override
	@GetMapping("/{cartId}")
	public ResponseEntity<RsData<CartResponse>> getCart(@PathVariable("cartId") Long cartId,
		@Parameter(hidden = true) @CurrentMemberId Long memberId) {
		CartResponse response = cartService.getCart(cartId, memberId);
		return ResponseEntity.ok(RsData.success(response));
	}

	@Override
	@GetMapping
	public ResponseEntity<RsData<CartResponse>> getMyCart(@Parameter(hidden = true) @CurrentMemberId Long memberId) {
		CartResponse response = cartService.getMyCart(memberId);
		return ResponseEntity.ok(RsData.success(response));
	}
}
