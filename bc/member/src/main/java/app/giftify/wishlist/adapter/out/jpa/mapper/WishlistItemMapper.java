package app.giftify.wishlist.adapter.out.jpa.mapper;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistItemJpaEntity;
import app.giftify.wishlist.core.domain.WishlistItem;

public class WishlistItemMapper {

	public static WishlistItemJpaEntity toEntity(WishlistItem domain) {
		return WishlistItemJpaEntity.builder()
			.wishlistId(domain.getWishlistId())
			.productId(domain.getProductId())
			.wishlistItemStatus(domain.getWishlistItemStatus())
			.addedAt(domain.getAddedAt())
			.build();
	}

	public static WishlistItem toDomain(WishlistItemJpaEntity entity) {
		return WishlistItem.builder()
			.id(entity.getId())
			.wishlistId(entity.getWishlistId())
			.productId(entity.getProductId())
			.wishlistItemStatus(entity.getWishlistItemStatus())
			.addedAt(entity.getAddedAt())
			.build();
	}
}
