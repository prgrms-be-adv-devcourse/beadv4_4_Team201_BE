package app.giftify.settlement.domain;

import app.giftify.settlement.application.SettlementSource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_item")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
public class SettlementItem {
    // 식별
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SettlementItemType type;

    @Column(nullable = false)
    private Long originId;

    // 스냅샷에서 복사된 근거 값
    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long orderItemId;

    @Column(nullable = false)
    private Long fundingId;

    // 회계적 증빙 / 정산 근거 관점에서 필요한 값
    @Column(nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    @Column(nullable = false)
    private LocalDateTime paidAt;

    @Column(nullable = false)
    private LocalDateTime confirmedAt;

    @Embedded
    private SettlementCore core;

    @Embedded
    private LifeCycleMeta lifeCycleMeta;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public SettlementItem(Long sellerId, SettlementItemType type, Long orderId, Long orderItemId, Long fundingId, String orderNumber, LocalDateTime orderedAt, LocalDateTime paidAt, LocalDateTime confirmedAt, SettlementCore core, LifeCycleMeta lifeCycleMeta) {
        this.sellerId = sellerId;
        this.type = type;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.fundingId = fundingId;
        this.orderNumber = orderNumber;
        this.orderedAt = orderedAt;
        this.paidAt = paidAt;
        this.confirmedAt = confirmedAt;
        this.core = core;
        this.lifeCycleMeta = lifeCycleMeta;
    }

    public static SettlementItem createPaymentItem(SettlementSource source, SettlementCore core, LocalDateTime confirmedAt) {
        return new SettlementItem(
                source.getSellerId(),
                SettlementItemType.ITEM_PAYMENT,
                source.getOrderId(),
                source.getOrderItemId(),
                source.getFunding(),
                source.getOrderNumber(),
                source.getOrderedAt(),
                source.getPaidAt(),
                confirmedAt,
                core,
                LifeCycleMeta.of(confirmedAt)
        );
    }
}
