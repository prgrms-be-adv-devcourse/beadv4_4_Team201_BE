package app.giftify.wishlist.adapter.in.web.responseDto;

import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Builder
public record WishlistResponse(
        Long id,
        Long memberId,
        String nickname,
        Visibility visibility,
        LocalDateTime createdAt,
        Page<WishlistItemResponse> items
) {
    public static WishlistResponse from(Wishlist wishlist) {
        return WishlistResponse.builder()
                .id(wishlist.getId())
                .memberId(wishlist.getMemberId())
                .visibility(wishlist.getVisibility())
                .createdAt(wishlist.getCreatedAt())
                .build();
    }

    public static WishlistResponse from(Wishlist wishlist, String nickname, Page<WishlistItemResponse> items) {
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
