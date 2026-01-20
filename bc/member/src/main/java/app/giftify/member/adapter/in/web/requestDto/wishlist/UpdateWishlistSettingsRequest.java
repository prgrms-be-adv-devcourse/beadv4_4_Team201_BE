package app.giftify.member.adapter.in.web.requestDto.wishlist;

import jakarta.validation.constraints.NotBlank;

public record UpdateWishlistSettingsRequest(
        @NotBlank(message = "위시리스트 상태 정보 [PUBLIC/PRIVATE/FRIENDS_ONLY] 입력은 필수입니다.")
        String visibility
) {
}