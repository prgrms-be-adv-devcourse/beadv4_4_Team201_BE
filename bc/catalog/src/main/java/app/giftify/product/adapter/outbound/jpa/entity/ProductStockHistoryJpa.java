package app.giftify.product.adapter.outbound.jpa.entity;

import app.giftify.product.domain.StockChangeType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Immutable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "PRODUCT_STOCK_HISTORY")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class ProductStockHistoryJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockChangeType changeType;

    @Column(nullable = false)
    private int delta;

    @Column(nullable = false)
    private int beforeStock;

    @Column(nullable = false)
    private int afterStock;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;
}
