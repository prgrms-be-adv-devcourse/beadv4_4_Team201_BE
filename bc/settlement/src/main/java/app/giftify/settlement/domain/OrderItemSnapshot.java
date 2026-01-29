package app.giftify.settlement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item_snapshot")
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemSnapshot {
    @Id
    private Long orderItemId;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal totalAmount;
}
