package app.giftify.settlement.domain;

import app.giftify.settlement.application.SettlementSource;
import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.exception.DomainException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "settlement_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_target_id_type",
                columnNames = {"target_id", "type"}
        )
)
@NoArgsConstructor
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

    @Column
    private Long originId;

    // 스냅샷에서 복사된 근거 값
    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long orderItemId;

    @Column(nullable = false)
    private Long targetId;

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

    private SettlementItem(Long sellerId,
                           SettlementItemType type,
                           Long originId,
                           Long orderId,
                           Long orderItemId,
                           Long targetId,
                           String orderNumber,
                           LocalDateTime orderedAt,
                           LocalDateTime paidAt,
                           LocalDateTime confirmedAt,
                           SettlementCore core,
                           LifeCycleMeta lifeCycleMeta) {

        if (sellerId == null) {
            throw new DomainException(SettlementErrorCode.INVALID_SELLER_ID);
        }
        if (type == null) {
            throw new DomainException(SettlementErrorCode.INVALID_SETTLEMENT_TYPE);
        }
        if (core == null) {
            throw new DomainException(SettlementErrorCode.INVALID_SETTLEMENT_CORE);
        }
        if (lifeCycleMeta == null) {
            throw new DomainException(SettlementErrorCode.INVALID_LIFECYCLE_META);
        }
        if (orderedAt == null || paidAt == null || confirmedAt == null) {
            throw new DomainException(SettlementErrorCode.INVALID_TIME_SEQUENCE);
        }
        if (type.requiresOriginId() && originId == null) {
            throw new DomainException(SettlementErrorCode.INVALID_ORIGIN_ID);
        }
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new DomainException(SettlementErrorCode.INVALID_ORDER_NUMBER);
        }

        validateTimeSequence(orderedAt, paidAt, confirmedAt);

        this.sellerId = sellerId;
        this.type = type;
        this.originId = originId;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.targetId = targetId;
        this.orderNumber = orderNumber;
        this.orderedAt = orderedAt;
        this.paidAt = paidAt;
        this.confirmedAt = confirmedAt;
        this.core = core;
        this.lifeCycleMeta = lifeCycleMeta;
    }

    private SettlementItem(Long sellerId,
                           SettlementItemType type,
                           Long orderId,
                           Long orderItemId,
                           Long targetId,
                           String orderNumber,
                           LocalDateTime orderedAt,
                           LocalDateTime paidAt,
                           LocalDateTime confirmedAt,
                           SettlementCore core,
                           LifeCycleMeta lifeCycleMeta) {

        this(sellerId, type, null, orderId, orderItemId, targetId, orderNumber, orderedAt, paidAt, confirmedAt, core, lifeCycleMeta);
    }

    public static SettlementItem createPaymentItem(SettlementSource source, SettlementCore core, LocalDateTime confirmedAt) {
        if (source.getPaidAt() == null) {
            throw new DomainException(SettlementErrorCode.PAYMENT_NOT_COMPLETED);
        }

        if (confirmedAt == null) {
            throw new DomainException(SettlementErrorCode.CONFIRMED_AT_REQUIRED);
        }

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
                LifeCycleMeta.pending(confirmedAt)
        );
    }

    // todo: 추후 기능 추가
//    public void start() { this.lifeCycleMeta = lifeCycleMeta.start(); }
//    public void complete(LocalDateTime at) { this.lifeCycleMeta = lifeCycleMeta.complete(at); }
//    public void cancel(LocalDateTime at) { this.lifeCycleMeta = lifeCycleMeta.cancel(at); }

    private void validateTimeSequence(LocalDateTime orderedAt, LocalDateTime paidAt, LocalDateTime confirmedAt) {
        if (paidAt.isBefore(orderedAt)) {
            throw new DomainException(SettlementErrorCode.INVALID_TIME_SEQUENCE);
        }
        if (confirmedAt != null && confirmedAt.isBefore(paidAt)) {
            throw new DomainException(SettlementErrorCode.INVALID_TIME_SEQUENCE);
        }
    }
}
