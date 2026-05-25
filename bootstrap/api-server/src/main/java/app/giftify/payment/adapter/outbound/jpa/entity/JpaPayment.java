package app.giftify.payment.adapter.outbound.jpa.entity;

import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.payment.domain.type.PaymentType;
import app.giftify.support.common.money.Money;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaPayment extends BaseJpaEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 50)
    private PaymentMethod method;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_number", nullable = false, length = 255)
    private String orderNumber;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "origin_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal originAmount;

    @Column(name = "paid_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "refunded_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal refundedAmount;

    @Column(name = "wallet_deducted_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal walletDeductedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PaymentStatus status;

    @Column(name = "payment_key", length = 255)
    private String paymentKey;

    @Column(name = "last_transaction_key", length = 255)
    private String lastTransactionKey;

    @Column(name = "approve_code", length = 255)
    private String approveCode;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Version
    private Long version;

    private JpaPayment(
            PaymentType type,
            PaymentMethod method,
            Long orderId,
            String orderNumber,
            Long memberId,
            BigDecimal originAmount,
            BigDecimal paidAmount,
            BigDecimal refundedAmount,
            BigDecimal walletDeductedAmount,
            PaymentStatus status,
            String paymentKey,
            String lastTransactionKey,
            String approveCode,
            LocalDateTime paidAt
    ) {
        this.type = type;
        this.method = method;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.memberId = memberId;
        this.originAmount = originAmount;
        this.paidAmount = paidAmount;
        this.refundedAmount = refundedAmount;
        this.walletDeductedAmount = walletDeductedAmount;
        this.status = status;
        this.paymentKey = paymentKey;
        this.lastTransactionKey = lastTransactionKey;
        this.approveCode = approveCode;
        this.paidAt = paidAt;
    }

    public static JpaPayment from(Payment payment) {


        return new JpaPayment(
                payment.getType(),
                payment.getMethod(),
                payment.getOrderId(),
                payment.getOrderNumber(),
                payment.getMemberId(),
                payment.getOriginAmount().amount(),
                payment.getPaidAmount().amount(),
                payment.getRefundedAmount().amount(),
                payment.getWalletDeductedAmount().amount(),
                payment.getStatus(),
                payment.getPaymentKey(),
                payment.getLastTransactionKey(),
                payment.getApproveCode(),
                payment.getPaidAt()
        );
    }

    public Payment toDomain() {


        return Payment.builder()
                .id(super.getId())
                .type(type)
                .method(method)
                .orderId(orderId)
                .orderNumber(orderNumber)
                .memberId(memberId)
                .originAmount(Money.of(originAmount))
                .paidAmount(Money.of(paidAmount))
                .refundedAmount(Money.of(refundedAmount))
                .walletDeductedAmount(Money.of(walletDeductedAmount))
                .status(status)
                .paymentKey(paymentKey)
                .lastTransactionKey(lastTransactionKey)
                .approveCode(approveCode)
                .paidAt(paidAt)
                .createdAt(super.getCreatedAt())
                .build();
    }

    public void updateFrom(Payment payment) {
        this.status = payment.getStatus();
        this.paymentKey = payment.getPaymentKey();
        this.lastTransactionKey = payment.getLastTransactionKey();
        this.approveCode = payment.getApproveCode();
        this.paidAt = payment.getPaidAt();
        this.refundedAmount = payment.getRefundedAmount().amount();
        this.walletDeductedAmount = payment.getWalletDeductedAmount().amount();
    }
}
