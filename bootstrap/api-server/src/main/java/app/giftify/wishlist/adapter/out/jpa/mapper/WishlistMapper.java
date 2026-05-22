package app.giftify.wishlist.adapter.out.jpa.mapper;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistJpaEntity;
import app.giftify.wishlist.core.domain.Wishlist;

public class WishlistMapper {

    public static WishlistJpaEntity toEntity(Wishlist domain) {
        if (domain == null) {
            return null;
        }

        return new WishlistJpaEntity(
                domain.getId(),
                domain.getMemberId(),
                domain.getVisibility()
        );
    }

    public static Wishlist toDomain(WishlistJpaEntity entity) {
        return Wishlist.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .visibility(entity.getVisibility())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
