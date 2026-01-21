package app.giftify.payment.adapter.out.jpa.entity.settlement;

import domain.settlement.SettlementStatus;
import domain.settlement.SettlementType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "SETTLEMENT_ITEM")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JpaSettlementItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;                   // 결제/주문 식별자(현재는 결제)

    @Column(nullable = false, unique = true)
    private String paymentKey;              // PG 결제 식별자

//    private String transactionKey;          // PG 트랜잭션 식별자(멱등키) (todo: 토스 대조 미정)

    @Column(nullable = false)
    private Long sellerId;                  // 판매자 식별자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementType type;            // PAYMENT, CANCEL

    @Column(nullable = false)
    private BigDecimal totalAmount;              // 판매 금액(₩)

    @Column(nullable = false)
    private BigDecimal platformFee;              // 우리 수수료(₩)

    @Column(nullable = false)
    private BigDecimal pgFee;                    // pg 수수료(₩)

    @Column(nullable = false)
    private BigDecimal settlementAmount;         // 정산 금액(₩) (totalAmount - platformFee - pgFee)

    @Column(nullable = false)
    private SettlementStatus status;        // READY, WAIT, COMPLETE

    @Column(nullable = false)
    private LocalDateTime settlementDate;   // 정산 예정일

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void updateStatus(SettlementStatus status) {
        this.status = status;
    }
}


