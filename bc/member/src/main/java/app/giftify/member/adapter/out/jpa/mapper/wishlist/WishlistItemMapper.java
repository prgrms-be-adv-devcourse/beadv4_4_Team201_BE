package app.giftify.member.adapter.out.jpa.mapper.wishlist;

import app.giftify.member.adapter.out.jpa.entity.wishlist.WishlistItemJpaEntity;
import app.giftify.member.core.domain.wishlist.WishlistItem;

public class WishlistItemMapper {

    public static WishlistItemJpaEntity toEntity(WishlistItem domain) {
        return WishlistItemJpaEntity.builder()
                .id(domain.getId())
                .wishlistId(domain.getWishlistId())
                .productId(domain.getProductId())
                .itemStatus(domain.getItemStatus())
                .build();
    }

    public static WishlistItem toDomain(WishlistItemJpaEntity entity) {
        return WishlistItem.builder()
                .id(entity.getId())
                .wishlistId(entity.getWishlistId())
                .productId(entity.getProductId())
                .itemStatus(entity.getItemStatus())
                .build();
    }
}
