package app.giftify.wishlist.adapter.in.web.responseDto;

import app.giftify.support.common.api.paging.PageResponse;
import app.giftify.wishlist.application.port.in.WishlistOverview;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record WishlistResponse(
        Long id,
        Long memberId,
        String nickname,
        Visibility visibility,
        LocalDateTime createdAt,
        PageResponse<WishlistItemResponse> items
) {
    public static WishlistResponse from(Wishlist wishlist) {
        return WishlistResponse.builder()
                .id(wishlist.getId())
                .memberId(wishlist.getMemberId())
                .visibility(wishlist.getVisibility())
                .createdAt(wishlist.getCreatedAt())
                .build();
    }

    public static WishlistResponse from(WishlistOverview overview, int page, int size) {
        var itemDetails = overview.itemDetails();
        var items = itemDetails.content().stream()
                .map(WishlistItemResponse::from)
                .toList();
        PageResponse<WishlistItemResponse> itemPage = PageResponse.of(items, page, size, itemDetails.totalElements());

        return WishlistResponse.builder()
                .id(overview.wishlist().getId())
                .memberId(overview.wishlist().getMemberId())
                .nickname(overview.ownerNickname())
                .visibility(overview.wishlist().getVisibility())
                .createdAt(overview.wishlist().getCreatedAt())
                .items(itemPage)
                .build();
    }
}
