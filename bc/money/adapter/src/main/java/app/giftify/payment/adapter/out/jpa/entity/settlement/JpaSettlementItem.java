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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Getter
public class JpaSettlementItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sellerId;

    private Long settlementId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private Long orderItemId;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    private String paymentKey;
    private String transactionKey;

    @Enumerated(EnumType.STRING)
    private PaymentMethodType paymentMethodType;

    @Column(precision = 19, scale = 4)
    private BigDecimal platformFee;

    @Column(precision = 19, scale = 4)
    private BigDecimal pgFee;

    @Column(precision = 19, scale = 4)
    private BigDecimal settlementItemAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementItemType type;

    private Long originId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementItemStatus status;

    private LocalDate expectedDate;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 도메인 생성 시점(PENDING)에 맞춘 생성자
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

    public static JpaSettlementItem from(SettlementItem domain) {
        return new JpaSettlementItem(
                domain.getSellerId(),
                domain.getOrderInfo(),
                domain.getType(),
                domain.getStatus()
        );
    }
}


