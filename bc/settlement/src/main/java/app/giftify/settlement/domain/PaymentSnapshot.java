package app.giftify.settlement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_snapshot")
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSnapshot {
    @Id
    private Long paymentId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false, unique = true)
    private String paymentKey;

    @Column(nullable = false, unique = true)
    private String transactionKey;

    @Column(nullable = false)
    private LocalDateTime paidAt;

    @Column(nullable = false)
    private BigDecimal paidAmount;
}
