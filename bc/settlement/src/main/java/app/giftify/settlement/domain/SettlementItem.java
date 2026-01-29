package app.giftify.settlement.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_item")
@NoArgsConstructor
@AllArgsConstructor
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
    private String paymentKey;

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
}
