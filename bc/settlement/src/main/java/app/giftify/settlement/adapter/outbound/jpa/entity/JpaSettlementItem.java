package app.giftify.settlement.adapter.outbound.jpa.entity;

import app.giftify.settlement.domain.*;
import app.giftify.shared.domain.type.PaymentMethodType;
import app.giftify.shared.domain.vo.OrderItemInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Column(nullable = false, unique = true)
    private Long orderItemId;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    private String paymentKey;

    private String transactionKey;

    private LocalDateTime paidAt;

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

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    private LocalDate expectedDate;

    private LocalDate settledAt;

    private LocalDateTime cancelledAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 도메인 생성 시점(PENDING)에 맞춘 생성자
    private JpaSettlementItem(Long sellerId, OrderItemInfo orderItemInfo, SettlementItemType type, SettlementItemStatus status, LocalDateTime occurredAt) {
        this.sellerId = sellerId;
        this.orderId = orderItemInfo.orderId();
        this.orderNumber = orderItemInfo.orderNumber();
        this.quantity = orderItemInfo.quantity();
        this.orderItemId = orderItemInfo.orderItemId();
        this.totalAmount = orderItemInfo.totalAmount().amount();
        this.orderedAt = orderItemInfo.orderedAt();
        this.type = type;
        this.status = status;
        this.occurredAt = occurredAt;
    }

    public static JpaSettlementItem from(SettlementItem domain) {
        return new JpaSettlementItem(
                domain.getSellerId(),
                domain.getOrderItemInfo(),
                domain.getType(),
                domain.getStatus(),
                domain.getOccurredAt()
        );
    }
}


