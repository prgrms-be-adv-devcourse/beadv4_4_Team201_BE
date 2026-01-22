package app.giftify.wishlist.adapter.in.web.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.security.common.context.AuthenticatedMember;
import app.giftify.wishlist.adapter.in.web.responseDto.WishlistItemResponse;
import app.giftify.wishlist.application.port.in.AddWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.GetWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.RemoveWishlistItemUseCase;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/wishlist/items")
@RequiredArgsConstructor
@Validated
public class WishlistItemController {
	private final AddWishlistItemUseCase addWishlistItemUseCase;
	private final GetWishlistItemUseCase getWishlistItemUseCase;
	private final RemoveWishlistItemUseCase removeWishlistItemUseCase;

	// 위시리스트에 담긴 모든 상품 조회
	@GetMapping("/me")
	public ResponseEntity<List<WishlistItemResponse>> getAllProducts(
		@AuthenticatedMember String authSub
	) {
		List<WishlistItemResponse> items = getWishlistItemUseCase.getWishlistItems(authSub)
			.stream()
			.map(WishlistItemResponse::from)
			.collect(Collectors.toList());
		return ResponseEntity.ok(items);
	}

	// 특정 상품이 위시리스트에 포함되어 있는지 확인
	@GetMapping("/me/check")
	public ResponseEntity<?> isExistProduct(
		@AuthenticatedMember String authSub,
		@RequestParam(name = "productId") Long productId
	) {
		boolean exists = getWishlistItemUseCase.isItemExists(authSub, productId);

		return ResponseEntity.ok(exists);
	}

	@PostMapping("/add")
	public ResponseEntity<WishlistItemResponse> addProduct(
		@AuthenticatedMember String authSub,
		@RequestParam(name = "productId") Long productId
	) {
		// command 생성
		AddWishlistItemUseCase.WishlistItemAddCommand command = new AddWishlistItemUseCase.WishlistItemAddCommand(
			authSub,
			productId,
			WishlistItemStatus.PENDING
		);

		// 비즈니스 로직(중복 체크, 판매 상태 검증 등)은 서비스 계층에서 수행
		return ResponseEntity.ok(WishlistItemResponse.from(addWishlistItemUseCase.addWishlistItem(command)));
	}

	// 위시리스트에서 특정 상품 삭제
	@DeleteMapping("/remove")
	public ResponseEntity<Void> removeProduct(
		@AuthenticatedMember String authSub,
		@RequestParam(name = "productId") Long productId
	) {
		RemoveWishlistItemUseCase.WishlistItemRemoveCommand command = new RemoveWishlistItemUseCase.WishlistItemRemoveCommand(
			authSub,
			productId
		);

		removeWishlistItemUseCase.removeWishlistItem(command);

		return ResponseEntity.noContent().build();
	}
}
