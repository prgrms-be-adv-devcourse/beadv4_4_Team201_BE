package app.giftify.cart.adapter.outbound.jpa;

import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JpaCartItem> items = new ArrayList<>();

    private JpaCart(Long memberId) {
        this.memberId = memberId;
    }

    private JpaCart(Long id, Long memberId) {
        super(id);  // BaseJpaEntity(Long id) 호출 ← 여기서 ID 설정!
        this.memberId = memberId;
        // items는 비어있음 (나중에 addItem()으로 추가)
    }

    public static JpaCart from(Long id, Long memberId) {
        return new JpaCart(id, memberId);
    }

    // ==== 연관관계 편의 메서드 ====
    public void addItem(JpaCartItem item) {
        this.items.add(item);
        item.setCart(this);
    }

    public void removeItem(JpaCartItem item) {
        items.remove(item);
        item.setCart(null);
    }

    // 방어적 복사
    public List<JpaCartItem> getItems() {
        return new ArrayList<>(items);
    }
}