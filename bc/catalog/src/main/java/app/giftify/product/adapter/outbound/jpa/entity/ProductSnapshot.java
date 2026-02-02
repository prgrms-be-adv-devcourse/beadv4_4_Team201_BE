package app.giftify.product.adapter.outbound.jpa.entity;

import app.giftify.replica.member.Member;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static app.giftify.product.domain.ProductStatus.ACTIVE;

@Entity
@Table(name = "PRODUCT_SNAPSHOT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductSnapshot extends BaseJpaEntity {

    @Column(nullable = false)
    private Long originalProductId; // 원본 상품 ID

    @Column(nullable = false)
    private Long sellerId; // 주문 당시 판매자 ID

    @Column(nullable = false)
    private String sellerNickname; // 주문 당시 판매자 닉네임

    @Column(nullable = false)
    private String name; // 주문 당시 상품명

    private String description; // 주문 당시 설명

    @Column(nullable = false)
    private int price; // 주문 당시 가격

    @Column(nullable = false)
    private boolean onSale; // 주문 당시 구매가능여부

    @Builder
    public ProductSnapshot(
            Long originalProductId,
            Long sellerId,
            String sellerNickname,
            String name,
            String description,
            int price,
            boolean onSale
    ) {
        this.originalProductId = originalProductId;
        this.sellerId = sellerId;
        this.sellerNickname = sellerNickname;
        this.name = name;
        this.description = description;
        this.price = price;
        this.onSale = onSale;
    }

    public static ProductSnapshot from(ProductJpa productJpa, Member seller) {
        return ProductSnapshot.builder()
                .originalProductId(productJpa.getId())
                .sellerId(productJpa.getSellerId())
                .sellerNickname(seller.getNickname())
                .name(productJpa.getName())
                .description(productJpa.getDescription())
                .price(productJpa.getPrice())
                .onSale(productJpa.getStatus() == ACTIVE && productJpa.getStock() != 0)
                .build();
    }
}
