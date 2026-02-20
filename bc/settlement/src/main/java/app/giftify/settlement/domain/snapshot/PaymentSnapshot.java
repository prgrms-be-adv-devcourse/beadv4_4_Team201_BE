package app.giftify.settlement.domain.snapshot;

import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.vo.Money;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_snapshot")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PaymentSnapshot {
    @Id
    private Long paymentId;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Column(nullable = false, unique = true)
    private String paymentKey;

    @Column(nullable = false, unique = true)
    private String transactionKey;

    @Column(nullable = false)
    private LocalDateTime paidAt;

    @Column(nullable = false)
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "paid_amount", nullable = false))
    private Money paidAmount;

    @Column
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;
}
