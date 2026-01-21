package app.giftify.wishlist.adapter.out.jpa.entity;

import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist_product_replica")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WishlistProductReplicaJpaEntity extends BaseJpaEntity {

    @Column(nullable = false, unique = true)
    private Long productId;

    @Column(nullable = false)
    private boolean wishlistAllowed;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private String name;
    private int price;
    private String sellerNickName;

    @Builder
    public WishlistProductReplicaJpaEntity(Long productId, boolean wishlistAllowed, LocalDateTime updatedAt,
                                           String name, int price, String sellerNickName) {
        this.productId = productId;
        this.wishlistAllowed = wishlistAllowed;
        this.updatedAt = updatedAt;
        this.name = name;
        this.price = price;
        this.sellerNickName = sellerNickName;
    }

    public void update(boolean wishlistAllowed, LocalDateTime updatedAt, String name, int price, String sellerNickName) {
        this.wishlistAllowed = wishlistAllowed;
        this.updatedAt = updatedAt;
        this.name = name;
        this.price = price;
        this.sellerNickName = sellerNickName;
    }
}