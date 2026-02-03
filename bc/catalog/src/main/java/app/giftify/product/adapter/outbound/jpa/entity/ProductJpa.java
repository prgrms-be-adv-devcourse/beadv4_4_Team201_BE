package app.giftify.product.adapter.outbound.jpa.entity;

import app.giftify.product.domain.ProductStatus;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PRODUCT")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductJpa extends BaseJpaEntity {

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    // 기존 엔티티 복원용 (ID 포함)
    public ProductJpa(Long id, Long sellerId, String name, String description,
                      int price, int stock, ProductStatus status) {
        super(id);
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.status = status;
    }
}
