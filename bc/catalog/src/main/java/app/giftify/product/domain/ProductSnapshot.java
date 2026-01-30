package app.giftify.product.domain;

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

    public static ProductSnapshot from(Product product, Member seller) {
        return ProductSnapshot.builder()
                .originalProductId(product.getId())
                .sellerId(product.getSellerId())
                .sellerNickname(seller.getNickname())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .onSale(product.getStatus() == ACTIVE && product.getStock() != 0)
                .build();
    }
}
