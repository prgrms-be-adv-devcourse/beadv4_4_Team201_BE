package app.giftify.wishlist.adapter.out.jpa.mapper;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistJpaEntity;
import app.giftify.wishlist.core.domain.Wishlist;

public class WishlistMapper {

	public static WishlistJpaEntity toEntity(Wishlist domain) {
		return WishlistJpaEntity.builder()
			.visibility(domain.getVisibility())
			.memberId(domain.getMemberId())
			.build();
	}

	public static Wishlist toDomain(WishlistJpaEntity entity) {
		return Wishlist.builder()
			.id(entity.getId())
			.memberId(entity.getMemberId())
			.visibility(entity.getVisibility())
			.build();
	}
}
