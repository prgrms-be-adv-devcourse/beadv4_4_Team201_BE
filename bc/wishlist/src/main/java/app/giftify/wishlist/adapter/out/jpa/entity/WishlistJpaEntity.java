package app.giftify.wishlist.adapter.out.jpa.entity;

import app.giftify.support.jpa.BaseJpaEntity;
import app.giftify.wishlist.core.domain.Visibility;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "wishlists")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class WishlistJpaEntity extends BaseJpaEntity {

    @Column(nullable = false, unique = true)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility;

    public WishlistJpaEntity(Long id, Long memberId, Visibility visibility) {
        super(id);
        this.memberId = memberId;
        this.visibility = visibility;
    }
}
