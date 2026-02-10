package app.giftify.product.adapter.outbound.jpa.entity;

import app.giftify.product.domain.ProductCategory;
import app.giftify.product.domain.ProductStatus;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private ProductCategory category;
    // Note :: 대표 이미지 한장, 추후 상세 이미지를 추가하면 List 로 관리해야 할 필요 있음
    // NOSONAR
    @Column(length = 500)
    private String imageKey;

    public ProductJpa(Long id, Long sellerId, String name, String description,
                      int price, int stock, ProductStatus status, ProductCategory category, String imageKey) {
        super(id);
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.status = status;
        this.category = category;
        this.imageKey = imageKey;
    }
}
