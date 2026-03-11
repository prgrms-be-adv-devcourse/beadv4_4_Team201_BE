package app.giftify.settlement.domain.model;

import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.status.SettlementQueueStatus;
import app.giftify.shared.api.exception.DomainException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "settlement_queues",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "settlement_item_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_item_id")
    private SettlementItem item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementQueueStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public SettlementQueue(SettlementItem item) {
        this.item = item;
        this.status = SettlementQueueStatus.READY;
    }

    public void done(Long historyId) {
        if (this.status != SettlementQueueStatus.IN_PROGRESS) {
            throw new DomainException(SettlementErrorCode.INVALID_STATUS_TRANSITION);
        }

        this.status = SettlementQueueStatus.DONE;
        item.complete(historyId);
    }

    public void startProcessing() {
        if (this.status != SettlementQueueStatus.READY) {
            throw new DomainException(SettlementErrorCode.INVALID_STATUS_TRANSITION);
        }

        this.status = SettlementQueueStatus.IN_PROGRESS;
    }

    public void fail() {
        this.status = SettlementQueueStatus.FAIL;
    }
}
