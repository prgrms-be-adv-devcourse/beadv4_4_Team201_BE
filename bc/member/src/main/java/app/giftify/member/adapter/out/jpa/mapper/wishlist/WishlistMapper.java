package app.giftify.member.adapter.out.jpa.mapper.wishlist;

import app.giftify.member.adapter.out.jpa.entity.wishlist.WishlistJpaEntity;
import app.giftify.member.core.domain.wishlist.Wishlist;

public class WishlistMapper {

    public static WishlistJpaEntity toEntity(Wishlist domain) {
        return WishlistJpaEntity.builder()
                .id(domain.getId())
                .authSub(domain.getAuthSub())
                .visibility(domain.getVisibility())
                .memberId(domain.getMemberId())
                .build();
    }

    public static Wishlist toDomain(WishlistJpaEntity entity) {
        return Wishlist.builder()
                .id(entity.getId())
                .authSub(entity.getAuthSub())
                .memberId(entity.getMemberId())
                .visibility(entity.getVisibility())
                .build();
    }
}
