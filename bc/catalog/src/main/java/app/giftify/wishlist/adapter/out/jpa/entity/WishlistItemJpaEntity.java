package app.giftify.wishlist.adapter.out.jpa.entity;

import app.giftify.support.jpa.BaseJpaEntity;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist_item")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class WishlistItemJpaEntity extends BaseJpaEntity {

    @Column(name = "wishlist_id", nullable = false)
    private Long wishlistId;

    @Column(nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WishlistItemStatus wishlistItemStatus;

    private LocalDateTime addedAt;
}
