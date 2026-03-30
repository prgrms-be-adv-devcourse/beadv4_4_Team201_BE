package app.giftify.payment.domain;

import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.event.payment.*;
import app.giftify.shared.domain.type.CancelType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;
import org.springframework.lang.CheckReturnValue;

import java.time.LocalDateTime;
import java.util.Objects;

public class Payment extends BaseDomainModel {

    // ========== 필드 ========== //

    private final PaymentType type;
    private final PaymentMethod method;
    private final Long orderId;
    private final String orderNumber;
    private final Long memberId;
    private final Money originAmount; // FIXME :: originAmount 의미 재정의 + PaymentAmountInfo VO 도입
    private final Money paidAmount;
    private final Money refundedAmount;
    private final Money walletDeductedAmount;

    private final PaymentStatus status;
    private final String paymentKey;
    private final String lastTransactionKey;
    private final String approveCode;

    // ========== 감사용 필드 - 도메인이 스냅샷으로 들고 있되, 생성은 JPA에 위임 ========== //

    private final LocalDateTime paidAt;
    private final LocalDateTime createdAt;

    // ========== 생성자 ========== //

    private Payment(Long id, PaymentType type, PaymentMethod method,
                    Long orderId, String orderNumber, Long memberId,
                    Money originAmount, Money paidAmount, Money refundedAmount, Money walletDeductedAmount,
                    PaymentStatus status, String paymentKey, String lastTransactionKey, String approveCode,
                    LocalDateTime paidAt, LocalDateTime createdAt
    ) {
        super(id);
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.memberId = memberId;
        this.type = type;
        this.method = method;
        this.originAmount = originAmount;
        this.paidAmount = paidAmount;
        this.refundedAmount = refundedAmount != null ? refundedAmount : Money.zero();
        this.walletDeductedAmount = walletDeductedAmount != null ? walletDeductedAmount : Money.zero();
        this.status = status;
        this.paymentKey = paymentKey;
        this.lastTransactionKey = lastTransactionKey;
        this.approveCode = approveCode;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
    }

    // ========== static 팩토리 메서드 ========== //

    public static PaymentBuilder builder() {
        return new PaymentBuilder();
    }

    static Payment fromBuilder(
            Long id, PaymentType type, PaymentMethod method,
            Long orderId, String orderNumber, Long memberId,
            Money originAmount, Money paidAmount, Money refundedAmount, Money walletDeductedAmount,
            PaymentStatus status, String paymentKey, String lastTransactionKey, String approveCode,
            LocalDateTime paidAt, LocalDateTime createdAt
    ) {
        return new Payment(
                id, type, method,
                orderId, orderNumber, memberId,
                originAmount, paidAmount, refundedAmount, walletDeductedAmount,
                 status, paymentKey, lastTransactionKey, approveCode,
                paidAt, createdAt
        );
    }

    public static Payment createForDepositCharge(
            PaymentCreateContext context,
            Money amount
    ) {
        return builder()
                .orderId(context.orderId())
                .orderNumber(context.orderNumber())
                .memberId(context.memberId())
                .type(context.type())
                .method(context.method())
                .originAmount(amount)
                .paidAmount(amount)
                .status(PaymentStatus.PENDING)
                .build();
    }

    public static Payment create(
            PaymentCreateContext context,
            Money originAmount,
            Money paidAmount,
            Money walletDeductedAmount
    ) {
        return builder()
                .orderId(context.orderId())
                .orderNumber(context.orderNumber())
                .memberId(context.memberId())
                .type(context.type())
                .method(context.method())
                .originAmount(originAmount)
                .paidAmount(paidAmount)
                .walletDeductedAmount(walletDeductedAmount)
                .status(PaymentStatus.PENDING)
                .build();
    }

    // ========== 상태 전이 메서드 ========== //

    @CheckReturnValue
    public Payment complete(String paymentKey, String approveCode, String lastTransactionKey, LocalDateTime paidAt) {
        if (!PaymentEventType.PAID.canApply(this.status)) {
            throw new PaymentException(PaymentErrorCode.NOT_PAYABLE,
                    "[Payment] 결제 완료 불가능한 상태입니다: " + this.status);
        }

        Payment paid = new Payment(
                getId(), this.type, this.method,
                this.orderId, this.orderNumber, this.memberId,
                this.originAmount, this.paidAmount, this.refundedAmount,
                this.walletDeductedAmount,
                PaymentEventType.PAID.getResultStatus(),
                paymentKey, lastTransactionKey, approveCode,
                paidAt, this.createdAt
        );

        paid.registerEvent(PaymentSucceededEvent.create(
                new PaymentSuccessData(
                        getId(), getOrderId(), getMemberId(), getOrderNumber(), getPaidAmount(),
                        getMethod(), getType(), paymentKey, lastTransactionKey
                )
        ));

        return paid;
    }

    @CheckReturnValue
    public Payment cancel(CancelType cancelType, String reason) {
        Objects.requireNonNull(cancelType, "cancelType must not be null");

        PaymentEventType eventType = (cancelType == CancelType.REFUND)
                ? PaymentEventType.CANCEL_AFTER_PAID
                : PaymentEventType.CANCELED;

        if (!eventType.canApply(this.status)) {
            throw new PaymentException(PaymentErrorCode.NOT_CANCELABLE,
                    "[Payment] 취소 불가능한 상태입니다: " + this.status);
        }

        Payment canceled = new Payment(
                getId(), this.type, this.method,
                this.orderId, this.orderNumber, this.memberId,
                this.originAmount, this.paidAmount, this.refundedAmount,
                this.walletDeductedAmount,
                eventType.getResultStatus(),
                this.paymentKey, this.lastTransactionKey, this.approveCode,
                this.paidAt, this.createdAt
        );

        canceled.registerEvent(PaymentCanceledEvent.create(
                new PaymentCancelData(
                        getId(), getOrderId(), getMemberId(), getOrderNumber(), getPaidAmount(),
                        this.walletDeductedAmount,
                        getMethod(), getType(), cancelType, reason, this.lastTransactionKey
                )
        ));

        return canceled;
    }

    @CheckReturnValue
    public Payment partialCancel(String newTransactionKey, Money cancelAmount, CancelType cancelType, String reason) {
        Objects.requireNonNull(cancelAmount, "cancelAmount must not be null");

        Money newRefundedTotal = this.refundedAmount.plus(cancelAmount);

        PaymentEventType eventType = getCancelingEventType(newRefundedTotal);

        if (!eventType.canApply(this.status)) {
            throw new PaymentException(PaymentErrorCode.NOT_CANCELABLE,
                    "[Payment] 취소 불가능한 상태입니다: " + this.status);
        }

        Payment partiallyCanceled = new Payment(
                getId(), this.type, this.method,
                this.orderId, this.orderNumber, this.memberId,
                this.originAmount, this.paidAmount, newRefundedTotal,
                this.walletDeductedAmount,
                eventType.getResultStatus(),
                this.paymentKey, newTransactionKey, this.approveCode,
                this.paidAt, this.createdAt
        );

        partiallyCanceled.registerEvent(PaymentCanceledEvent.create(
                new PaymentCancelData(
                        getId(), getOrderId(), getMemberId(), getOrderNumber(),
                        cancelAmount, this.walletDeductedAmount,
                        getMethod(), getType(), cancelType, reason,
                        newTransactionKey
                )
        ));

        return partiallyCanceled;
    }

    @CheckReturnValue
    public Payment fail() {
        if (!PaymentEventType.FAILED.canApply(this.status)) {
            throw new PaymentException(PaymentErrorCode.NOT_FAILABLE,
                    "[Payment] 대기 중인 결제만 실패 처리할 수 있습니다. 현재 상태: " + this.status);
        }

        Payment failed = new Payment(
                getId(), this.type, this.method,
                this.orderId, this.orderNumber, this.memberId,
                this.originAmount, this.paidAmount, this.refundedAmount,
                this.walletDeductedAmount,
                PaymentEventType.FAILED.getResultStatus(),
                this.paymentKey, this.lastTransactionKey, this.approveCode,
                this.paidAt, this.createdAt
        );

        failed.registerEvent(PaymentFailedEvent.create(
                new PaymentFailureData(
                        getId(), getOrderId(), getMemberId(), getOrderNumber(), getPaidAmount(),
                        getWalletDeductedAmount(),
                        getMethod(), getType()
                )
        ));

        return failed;
    }

    @CheckReturnValue
    public Payment failCancel(String errorMetadata) {
        if (!PaymentEventType.CANCEL_FAILED.canApply(this.status)) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS,
                    "[Payment] 취소 실패 기록은 PAID 상태에서만 가능합니다. 현재 상태: " + this.status);
        }

        Payment cancelFailed = new Payment(
                getId(), this.type, this.method,
                this.orderId, this.orderNumber, this.memberId,
                this.originAmount, this.paidAmount, this.refundedAmount,
                this.walletDeductedAmount,
                this.status,
                this.paymentKey, this.lastTransactionKey, this.approveCode,
                this.paidAt, this.createdAt
        );

        cancelFailed.registerEvent(PaymentCancelFailedEvent.create(
                new PaymentCancelFailedData(
                        getId(), getOrderId(), getMemberId(), getOrderNumber(),
                        getMethod(), getType(), errorMetadata
                )
        ));

        return cancelFailed;
    }

    // ========== 상태 조회 메서드 ========== //

    // charged (PAID, PARTIALLY_CANCELED) -> REFUND, uncharged (PENDING) -> CANCEL
    public CancelType resolveCancelType() {
        return (this.status == PaymentStatus.PAID || this.status == PaymentStatus.PARTIALLY_CANCELED)
                ? CancelType.REFUND
                : CancelType.CANCEL;
    }

    public boolean isCancelable() {
        return PaymentEventType.CANCELED.canApply(this.status);
    }

    /**
     * 해당 회원이 이 결제의 소유자인지 확인합니다.
     *
     * @param requesterId 요청자 회원 ID
     * @return 소유자이면 true
     */
    public boolean isOwnedBy(Long requesterId) {
        return this.memberId != null && this.memberId.equals(requesterId);
    }

    // ========== private 헬퍼 ========== //

    private PaymentEventType getCancelingEventType(Money money) {
        PaymentEventType eventType;
        if (money.equals(this.paidAmount)) {
            eventType = (this.status == PaymentStatus.PARTIALLY_CANCELED)
                    ? PaymentEventType.FINAL_CANCEL
                    : PaymentEventType.CANCEL_AFTER_PAID;
        } else if (money.isLessThan(this.paidAmount)) {
            eventType = (this.status == PaymentStatus.PARTIALLY_CANCELED)
                    ? PaymentEventType.PARTIAL_CANCEL_AGAIN
                    : PaymentEventType.PARTIAL_CANCEL;
        } else {
            throw new PaymentException(PaymentErrorCode.CANCEL_AMOUNT_EXCEEDED,
                    "[Payment] 취소 금액이 결제 금액을 초과합니다.");
        }
        return eventType;
    }

    // ========== Getter ========== //

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Money getOriginAmount() {
        return originAmount;
    }

    public Money getPaidAmount() {
        return paidAmount;
    }

    public Money getRefundedAmount() {
        return refundedAmount;
    }

    public Money getWalletDeductedAmount() {
        return walletDeductedAmount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentType getType() {
        return type;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public String getLastTransactionKey() {
        return lastTransactionKey;
    }

    public String getApproveCode() {
        return approveCode;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ========== equals / hashCode ========== //

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Payment payment))
            return false;
        if (getId() != null && payment.getId() != null) {
            return Objects.equals(getId(), payment.getId());
        }
        return Objects.equals(orderNumber, payment.orderNumber);
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return Objects.hash(getId());
        }
        return Objects.hash(orderNumber);
    }
}
