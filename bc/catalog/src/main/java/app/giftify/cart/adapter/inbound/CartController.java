package app.giftify.cart.adapter.inbound;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import app.giftify.cart.application.inbound.AddCartItemCommand;
import app.giftify.cart.application.inbound.CartService;
import app.giftify.cart.core.domain.CartItemAddResult;
import app.giftify.cart.core.domain.CartItemKey;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.RsData;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/v2/carts")
@RequiredArgsConstructor
public class CartController implements CartV2ApiSpec {
	private final CartService cartService;

	@Override
	@PostMapping
	public ResponseEntity<RsData<Void>> addItemToMyCart(
			@CurrentMemberId Long memberId,
			@RequestBody CartItemRequest request
	) {
		CartItemAddResult result = cartService.upsertCartItem(memberId, new AddCartItemCommand(
				new CartItemKey(request.targetType(), request.targetId()),
				Money.of(request.amount())
		));

		if (result == CartItemAddResult.UPDATED) {
			return ResponseEntity.ok(RsData.success(null, "이미 장바구니에 있는 펀딩으로 가격이 수정되었습니다."));
		}
		return ResponseEntity.ok(RsData.success(null, "펀딩이 장바구니에 담겼습니다."));
	}

	@Override
	@PostMapping("/items")
	public ResponseEntity<RsData<Void>> addItemsToMyCart(
		@CurrentMemberId Long memberId,
		@RequestBody List<CartItemRequest> requests
	) {
		List<AddCartItemCommand> commands = requests.stream()
				.map(req -> new AddCartItemCommand(
						new CartItemKey(req.targetType(), req.targetId()),
						Money.of(req.amount())
				)).toList();

		cartService.upsertCartItems(memberId, commands);
		return ResponseEntity.ok(RsData.success(null, "펀딩 아이템들이 장바구니에 담겼습니다."));
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

	@Override
	@PatchMapping("/items")
	public ResponseEntity<RsData<Void>> updateItemsAmount(
		@CurrentMemberId Long memberId,
		@RequestBody List<CartItemRequest> requests
	) {
		List<AddCartItemCommand> commands = requests.stream()
						.map(c -> new AddCartItemCommand(
								new CartItemKey(c.targetType(), c.targetId()),
								Money.of(c.amount())
						)).toList();
		cartService.upsertCartItems(memberId, commands);
		return ResponseEntity.ok(RsData.success(null));
	}

	@Override
	@DeleteMapping("/items/{targetType}")
	public ResponseEntity<RsData<Void>> removeItems(
		@CurrentMemberId Long memberId,
		@PathVariable(value = "targetType") TargetType targetType,
		@RequestParam(value = "targetIds", required = false) List<Long> targetIds
	) {
		cartService.removeItems(memberId, targetType, targetIds);
		return ResponseEntity.ok(RsData.success(null));
	}

	@Override
	@DeleteMapping
	public ResponseEntity<RsData<Void>> clearCart(
		@CurrentMemberId Long memberId
	) {
		cartService.clearCart(memberId);
		return ResponseEntity.ok(RsData.success(null));
	}
}
