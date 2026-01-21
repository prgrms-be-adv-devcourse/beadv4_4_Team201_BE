package app.giftify.wishlist.adapter.in.web.responseDto;

import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record WishlistResponse(
        Long id,
        Long memberId,
        String authSub,
        Visibility visibility,
        LocalDate createdAt
) {
    public static WishlistResponse from(Wishlist wishlist) {
        return WishlistResponse.builder()
                .id(wishlist.getId())
                .memberId(wishlist.getMemberId())
                .authSub(wishlist.getAuthSub())
                .visibility(wishlist.getVisibility())
                .createdAt(wishlist.getCreatedAt())
                .build();
    }
}
