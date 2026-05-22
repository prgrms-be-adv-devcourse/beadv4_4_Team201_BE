package app.giftify.cart.adapter.outbound.jpa;

import app.giftify.wishlist.core.domain.WishlistItemStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "cart_items")
public class JpaCartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private JpaCart cart;

    @Column(name = "cart_id", insertable = false, updatable = false)
    private Long cartId;  // 읽기 전용 필드 (FK)

    private Long wishlistItemId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private WishlistItemStatus wishlistItemStatus;

    public static JpaCartItem from(
            Long id,
            Long wishlistItemId,
            BigDecimal amount
    ) {
        JpaCartItem item = new JpaCartItem();
        item.id = id;
        item.wishlistItemId = wishlistItemId;
        item.amount = amount;
        return item;
    }

    // ===== 연관관계 설정 메서드 =====
    void setCart(JpaCart cart) {
        this.cart = cart;
        this.cartId = cart != null ? cart.getId() : null;
    }
}