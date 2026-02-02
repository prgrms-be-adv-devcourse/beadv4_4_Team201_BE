package app.giftify.product.adapter.outbound.jpa.entity;

import app.giftify.product.domain.ProductStatus;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "PRODUCT")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductJpa extends BaseJpaEntity {
    private Long sellerId;
    private String name;
    private String description;
    private int price;
    private int stock;
    @Enumerated(EnumType.STRING)
    private ProductStatus status;
}
