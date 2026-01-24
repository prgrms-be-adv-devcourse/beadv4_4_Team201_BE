package app.giftify.payment.adapter.out.jpa.entity.settlement;

import app.giftify.shared.domain.type.PaymentMethodType;
import app.giftify.shared.domain.vo.OrderItemInfo;
import domain.settlement.SettlementItem;
import domain.settlement.SettlementItemStatus;
import domain.settlement.SettlementItemType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "SETTLEMENT_ITEM")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Getter
public class JpaSettlementItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sellerId;

    @Setter
    private Long settlementId;

    private Long orderId;

    private String orderNumber;

    private Long orderItemId;

    private Long quantity;

    private BigDecimal totalAmount;

    private LocalDateTime orderedAt;

    private String paymentKey;

    private String transactionKey;

    @Enumerated(EnumType.STRING)
    private PaymentMethodType paymentMethodType;

    private BigDecimal platformFee;

    private BigDecimal pgFee;

    private BigDecimal settlementItemAmount;

    @Enumerated(EnumType.STRING)
    private SettlementItemType type;

    private Long originId;

    private SettlementItemStatus status;

    private LocalDate expectedDate;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private JpaSettlementItem(Long sellerId, OrderItemInfo orderItemInfo, SettlementItemType type, SettlementItemStatus status) {
        this.sellerId = sellerId;
        this.orderId = orderItemInfo.orderId();
        this.orderNumber = orderItemInfo.orderNumber();
        this.quantity = orderItemInfo.quantity();
        this.orderItemId = orderItemInfo.orderItemId();
        this.totalAmount = orderItemInfo.totalAmount().amount();
        this.orderedAt = orderItemInfo.orderedAt();
        this.type = type;
        this.status = status;

        this.platformFee = BigDecimal.ZERO;
        this.pgFee = BigDecimal.ZERO;
        this.settlementItemAmount = BigDecimal.ZERO;
    }

    public static JpaSettlementItem from(SettlementItem settlementItem) {
        return new JpaSettlementItem(
                settlementItem.getSellerId(),
                settlementItem.getOrderInfo(),
                settlementItem.getType(),
                settlementItem.getStatus()
        );
    }
}


