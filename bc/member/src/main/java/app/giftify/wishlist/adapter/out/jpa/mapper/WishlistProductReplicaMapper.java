package app.giftify.wishlist.adapter.out.jpa.mapper;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistProductReplicaJpaEntity;
import app.giftify.wishlist.core.domain.replica.WishlistProductReplica;

public class WishlistProductReplicaMapper {

    public static WishlistProductReplica toDomain(WishlistProductReplicaJpaEntity entity) {
        if (entity == null) return null;
        return WishlistProductReplica.builder()
                .productId(entity.getProductId())
                .wishlistAllowed(entity.isWishlistAllowed())
                .updatedAt(entity.getUpdatedAt())
                .name(entity.getName())
                .price(entity.getPrice())
                .sellerNickName(entity.getSellerNickName())
                .build();
    }

    public static WishlistProductReplicaJpaEntity toEntity(WishlistProductReplica domain) {
        if (domain == null) return null;
        return WishlistProductReplicaJpaEntity.builder()
                .productId(domain.getProductId())
                .wishlistAllowed(domain.isWishlistAllowed())
                .updatedAt(domain.getUpdatedAt())
                .name(domain.getName())
                .price(domain.getPrice())
                .sellerNickName(domain.getSellerNickName())
                .build();
    }
}
