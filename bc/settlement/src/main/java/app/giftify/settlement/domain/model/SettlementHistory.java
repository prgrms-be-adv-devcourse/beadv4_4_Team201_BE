package app.giftify.settlement.domain.model;

import app.giftify.settlement.domain.status.SettlementHistoryStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "settlement_histories", uniqueConstraints = @UniqueConstraint(columnNames = {"seller_id", "settlement_date"}))
public class SettlementHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sellerId;

    @Embedded
    private SettlementAmountSummary amountSummary;

    @Column(nullable = false)
    private Integer itemCount;

    @Enumerated(EnumType.STRING)
    private SettlementHistoryStatus status;

    private Long referenceHistoryId;

    @Column(nullable = false)
    private LocalDate settlementDate;

    @CreatedDate
    private LocalDateTime settledAt;

    public SettlementHistory(Long sellerId, SettlementAmountSummary amountSummary, int itemCount) {
        this.sellerId = sellerId;
        this.amountSummary = amountSummary;
        this.itemCount = itemCount;
        this.status = SettlementHistoryStatus.CREATED;
        this.settlementDate = LocalDate.now();
    }
}
