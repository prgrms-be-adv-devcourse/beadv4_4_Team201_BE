package app.giftify.settlement.domain;

import app.giftify.shared.domain.type.PaymentMethodType;
import app.giftify.shared.domain.vo.Money;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

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
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "paid_amount", nullable = false))
    private Money paidAmount;

    @Column
    private PaymentMethodType method;
}
