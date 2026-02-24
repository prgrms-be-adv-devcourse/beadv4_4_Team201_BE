package app.giftify.settlement.domain.model;

import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.snapshot.OrderItemSnapshot;
import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "settlement_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_order_item_id_type",
                columnNames = {"order_item_id", "type"}
        ),
        indexes = {
                @Index(name = "idx_settlement_status_created_retry",
                        columnList = "status, created_at, retry_count"),
                @Index(name = "idx_settlement_order_id",
                        columnList = "order_id"),
                @Index(name = "idx_settlement_item_payment_id", columnList = "payment_id"),
                @Index(name = "idx_settlement_item_history_id", columnList = "history_id"),
                @Index(name = "idx_settlement_item_seller_id", columnList = "seller_id")
        }
)
@NoArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
public class SettlementItem extends BaseJpaEntity {
    // 핵심 식별자 및 연관 관계
    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long orderItemId;

    @Column(nullable = false)
    private Long paymentId;

    // 정산 대상 상세
    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    // 정산 정보 및 유형
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SettlementItemType type;

    @Embedded
    private SettlementCore core;

    // 상태 및 이력 관리
    @Embedded
    private ItemStatusInfo statusInfo;

    private Long historyId;

    private Long originId;

    // 시점 및 정책 데이터
    @Column(nullable = false)
    private LocalDateTime confirmedAt;

    @Column(nullable = false)
    private int retryCount = 0;

    public SettlementItem(Long originId, Long sellerId, SettlementItemType type, SettlementCore core,
                          ItemStatusInfo statusInfo, Long orderId, Long orderItemId, Long targetId,
                          TargetType targetType, Long paymentId, LocalDateTime confirmedAt) {
        super();
        this.originId = originId;
        this.sellerId = sellerId;
        this.type = type;
        this.core = core;
        this.statusInfo = statusInfo;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.paymentId = paymentId;
        this.confirmedAt = confirmedAt;
    }

    private static void validateCreatable(OrderItemSnapshot snapshot, SettlementCore core, SettlementItemType type, Long originId) {
        if (snapshot == null)
            throw new DomainException(SettlementErrorCode.MISSING_FIELD, "OrderItemSnapshot");
        if (core == null)
            throw new DomainException(SettlementErrorCode.MISSING_FIELD, "SettlementCore");
        if (type.requiresOriginId() && originId == null)
            throw new DomainException(SettlementErrorCode.MISSING_FIELD, "OriginID");
    }

    public static SettlementItem create(OrderItemSnapshot snapshot, SettlementCore core) {
        SettlementItemType type= SettlementItemType.ITEM_PAYMENT;

        validateCreatable(snapshot, core, type, null);

        return new SettlementItem(
                null,
                snapshot.sellerId(),
                type,
                core,
                ItemStatusInfo.create(snapshot.confirmedAt()),
                snapshot.orderId(),
                snapshot.orderItemId(),
                snapshot.targetId(),
                snapshot.targetType(),
                snapshot.paymentId(),
                snapshot.confirmedAt()
        );
    }

    public void validating() {
        statusInfo = statusInfo.validating();
    }

    public void failToValidate() {
        statusInfo = statusInfo.failToValidate();
        this.retryCount++;
    }

    public void ready() {
        statusInfo = this.statusInfo.ready();
    }

    public void processing() {
        statusInfo = this.statusInfo.processing();
    }

    public void failToExecute() {
        statusInfo = this.statusInfo.failToExecute();
    }

    public void complete(Long historyId) {
        this.historyId = historyId;
        statusInfo = statusInfo.complete();
    }

    public void manual() {
        statusInfo = statusInfo.manual();
    }
}
