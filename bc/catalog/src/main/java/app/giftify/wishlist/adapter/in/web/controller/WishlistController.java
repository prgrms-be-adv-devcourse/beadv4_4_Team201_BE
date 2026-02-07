package app.giftify.wishlist.adapter.in.web.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.RsData;
import app.giftify.wishlist.adapter.in.web.requestDto.UpdateWishlistSettingsRequest;
import app.giftify.wishlist.adapter.in.web.responseDto.WishlistResponse;
import app.giftify.wishlist.application.port.in.GetWishlistUseCase;
import app.giftify.wishlist.application.port.in.UpdateWishlistSettingsUseCase;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Validated
public class WishlistController {

	private final UpdateWishlistSettingsUseCase updateWishlistSettingsUseCase;
	private final GetWishlistUseCase getWishlistUseCase;

	// 위시리스트 조회
	// 현재 로그인한 사용자의 위시리스트 기본 정보 조회
	@GetMapping("/me")
	public ResponseEntity<WishlistResponse> getMyInfo(
		@CurrentMemberId Long memberId
	) {
		return ResponseEntity.ok(WishlistResponse.from(getWishlistUseCase.getOrCreateWishlistByMemberId(memberId)));
	}

	// PUBLIC 위시리스트 조회
	public ResponseEntity<RsData<List<WishlistResponse>>> getPublicWishList() {

		return null;
	}

	// 위시리스트 설정 변경
	// PUBLIC / PRIVATE / FRIENDS_ONLY
	@PatchMapping("/me/settings")
	public ResponseEntity<WishlistResponse> updateSettings(
		@CurrentMemberId Long memberId,
		@RequestBody @Valid UpdateWishlistSettingsRequest request
	) {
		Visibility visibility = Visibility.from(request.visibility());

		UpdateWishlistSettingsUseCase.UpdateSettingsCommand command = new UpdateWishlistSettingsUseCase.UpdateSettingsCommand(
			memberId,
			visibility
		);

		Wishlist updatedWishlist = updateWishlistSettingsUseCase.updateSettings(command);

		return ResponseEntity.ok(WishlistResponse.from(updatedWishlist));
	}
}
