package app.giftify.cart.adapter.outbound.jpa;

import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CART")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class JpaCart extends BaseJpaEntity {
    @Column(unique = true)
    private Long memberId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private List<JpaCartItem> items = new ArrayList<>();

    public JpaCart(Long memberId, List<JpaCartItem> items) {
        super();
        this.memberId = memberId;
        this.items = items != null ? items : new ArrayList<>();
    }

    // ==== 연관관계 편의 메서드 ====
    public void addItem(JpaCartItem item) {
        items.add(item);
        item.setCart(this);  // 양방향 연관관계 설정
    }

    public void removeItem(JpaCartItem item) {
        items.remove(item);
        item.setCart(null);
    }

    public static JpaCart from(Long memberId, List<JpaCartItem> items) {
        return new JpaCart(memberId, items);
    }

    // 방어적 복사
    public List<JpaCartItem> getItems() { return new ArrayList<>(items); }

}