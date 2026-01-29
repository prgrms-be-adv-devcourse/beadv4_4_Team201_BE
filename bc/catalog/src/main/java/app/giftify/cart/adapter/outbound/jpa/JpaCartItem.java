package app.giftify.cart.adapter.outbound.jpa;

import app.giftify.shared.domain.type.TargetType;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "cart_item")
public class JpaCartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private JpaCart cart;

    @Column(name = "cart_id", insertable = false, updatable = false)
    private Long cartId;  // 읽기 전용 필드 (FK)

    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    private Long targetId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private WishlistItemStatus wishlistItemStatus;

    public static JpaCartItem from(
            Long id,
            TargetType targetType,
            Long targetId,
            BigDecimal amount,
            WishlistItemStatus wishlistItemStatus
    ) {
        JpaCartItem item = new JpaCartItem();
        item.id = id;
        item.targetType = targetType;
        item.targetId = targetId;
        item.amount = amount;
        item.wishlistItemStatus = wishlistItemStatus;
        return item;
    }

    // ===== 연관관계 설정 메서드 =====
    void setCart(JpaCart cart) {
        this.cart = cart;
        this.cartId = cart != null ? cart.getId() : null;
    }
}