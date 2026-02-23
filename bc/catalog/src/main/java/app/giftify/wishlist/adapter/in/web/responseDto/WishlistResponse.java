package app.giftify.wishlist.adapter.in.web.responseDto;

import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record WishlistResponse(
        Long id,
        Long memberId,
        String nickname,
        Visibility visibility,
        LocalDateTime createdAt,
        List<WishlistItemResponse> items
) {
    public static WishlistResponse from(Wishlist wishlist) {
        return WishlistResponse.builder()
                .id(wishlist.getId())
                .memberId(wishlist.getMemberId())
                .visibility(wishlist.getVisibility())
                .createdAt(wishlist.getCreatedAt())
                .build();
    }

    public static WishlistResponse from(Wishlist wishlist, String nickname, List<WishlistItemResponse> items) {
        return WishlistResponse.builder()
                .id(wishlist.getId())
                .memberId(wishlist.getMemberId())
                .nickname(nickname)
                .visibility(wishlist.getVisibility())
                .createdAt(wishlist.getCreatedAt())
                .items(items)
                .build();
    }
}
