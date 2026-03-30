package app.giftify.payment.domain;

import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.event.payment.*;
import app.giftify.shared.domain.type.CancelType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Payment extends BaseDomainModel {
    private final PaymentType type;
    private final PaymentMethod method;
    private final Long orderId;
    private final String orderNumber;
    private final Long memberId;
    private final Money originAmount;
    private final Money paidAmount;
    private Money refundedAmount;
    private final Money walletDeductedAmount;
    private final List<OrderItemSnapshot> orderItems;

    private PaymentStatus status;
    private String paymentKey;
    private String lastTransactionKey;
    private String approveCode;
    // 도메인이 스냅샷으로 들고 있되, 생성은 JPA에 위임
    private LocalDateTime paidAt;
    private final LocalDateTime createdAt;

    private Payment(Long id, PaymentType type, PaymentMethod method,
                    Long orderId, String orderNumber, Long memberId,
                    Money originAmount, Money paidAmount, Money refundedAmount, Money walletDeductedAmount, List<OrderItemSnapshot> orderItems,
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
        this.orderItems = List.copyOf(orderItems);
        this.status = status;
        this.paymentKey = paymentKey;
        this.lastTransactionKey = lastTransactionKey;
        this.approveCode = approveCode;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
    }

    // ========== 정적 팩토리 메서드 ========== //

    public static Builder builder() {
        return new Builder();
    }

    // ========== 상태 변경 메서드 ========== //

    public void markAsPaid(String paymentKey, String approveCode, String lastTransactionKey, LocalDateTime paidAt) {
        if (!PaymentEventType.PAID.canApply(this.status)) {
            throw new PaymentException(PaymentErrorCode.NOT_PAYABLE,
                    "[Payment] 결제 완료 불가능한 상태입니다: " + this.status);
        }
        this.status = PaymentEventType.PAID.getResultStatus();
        this.paymentKey = paymentKey;
        this.approveCode = approveCode;
        this.lastTransactionKey = lastTransactionKey;
        this.paidAt = paidAt;

        registerEvent(PaymentSucceededEvent.create(
                PaymentEventData.forSuccess(
                        getId(), getOrderId(), getMemberId(), getOrderNumber(), getPaidAmount(),
                        getMethod(), getType(), paymentKey, lastTransactionKey
                )
        ));
    }

    // charged (PAID, PARTIALLY_CANCELED) -> REFUND, uncharged (PENDING) -> CANCEL
    public CancelType resolveCancelType() {
        return (this.status == PaymentStatus.PAID || this.status == PaymentStatus.PARTIALLY_CANCELED)
                ? CancelType.REFUND
                : CancelType.CANCEL;
    }

    public void markAsCanceled(CancelType cancelType, String reason) {
        PaymentEventType eventType = (cancelType == CancelType.REFUND)
                ? PaymentEventType.CANCEL_AFTER_PAID
                : PaymentEventType.CANCELED;

        if (!eventType.canApply(this.status)) {
            throw new PaymentException(PaymentErrorCode.NOT_CANCELABLE,
                    "[Payment] 취소 불가능한 상태입니다: " + this.status);
        }
        this.status = eventType.getResultStatus();

        registerEvent(PaymentCanceledEvent.create(
                PaymentEventData.forCancel(
                        getId(), getOrderId(), getMemberId(), getOrderNumber(), getPaidAmount(), this.walletDeductedAmount,
                        getMethod(), getType(), cancelType, reason, this.lastTransactionKey
                )
        ));
    }

    public void markAsPartiallyCanceled(String newTransactionKey, Money cancelAmount, CancelType cancelType, String reason) {
        Money newRefundedTotal = this.refundedAmount.plus(cancelAmount);

        PaymentEventType eventType = getCancelingEventType(newRefundedTotal);

        if (!eventType.canApply(this.status)) {
            throw new PaymentException(PaymentErrorCode.NOT_CANCELABLE,
                    "[Payment] 취소 불가능한 상태입니다: " + this.status);
        }

        this.status = eventType.getResultStatus();
        this.refundedAmount = newRefundedTotal;
        this.lastTransactionKey = newTransactionKey;

        registerEvent(PaymentCanceledEvent.create(
                PaymentEventData.forCancel(
                        getId(), getOrderId(), getMemberId(), getOrderNumber(),
                        cancelAmount, this.walletDeductedAmount,
                        getMethod(), getType(), cancelType, reason,
                        newTransactionKey
                )
        ));
    }

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

    public void markAsFailed(LocalDateTime occurredAt) {
        if (!PaymentEventType.FAILED.canApply(this.status)) {
            throw new PaymentException(PaymentErrorCode.NOT_FAILABLE,
                    "[Payment] 대기 중인 결제만 실패 처리할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = PaymentEventType.FAILED.getResultStatus();

        registerEvent(PaymentFailedEvent.create(
                PaymentEventData.forFailure(
                        getId(), getOrderId(), getMemberId(), getOrderNumber(), getPaidAmount(), getWalletDeductedAmount(),
                        getMethod(), getType()
                )
        ));
    }

    public void recordCancelFailed(String errorMetadata, LocalDateTime occurredAt) {
        if (!PaymentEventType.CANCEL_FAILED.canApply(this.status)) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS,
                    "[Payment] 취소 실패 기록은 PAID 상태에서만 가능합니다. 현재 상태: " + this.status);
        }

        registerEvent(PaymentCancelFailedEvent.create(
                PaymentEventData.forCancelFailed(
                        getId(), getOrderId(), getMemberId(), getOrderNumber(),
                        getMethod(), getType(), errorMetadata
                )
        ));
    }

    // ========== 상태 조회 메서드 ========== //

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

    public List<OrderItemSnapshot> getOrderItems() {
        return orderItems;
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

    // ========== Builder ========== //

    public static class Builder {
        private Long id;
        private Long orderId;
        private String orderNumber;
        private Long memberId;
        private Money originAmount;
        private Money paidAmount;
        private Money refundedAmount;
        private Money walletDeductedAmount;
        private List<OrderItemSnapshot> orderItems;
        private PaymentStatus status;
        private PaymentType type;
        private PaymentMethod method;
        private String paymentKey;
        private String lastTransactionKey;
        private String approveCode;
        private LocalDateTime paidAt;
        private LocalDateTime createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder orderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
            return this;
        }

        public Builder memberId(Long memberId) {
            this.memberId = memberId;
            return this;
        }

        public Builder originAmount(Money originAmount) {
            this.originAmount = originAmount;
            return this;
        }

        public Builder paidAmount(Money paidAmount) {
            this.paidAmount = paidAmount;
            return this;
        }

        public Builder refundedAmount(Money refundedAmount) {
            this.refundedAmount = refundedAmount;
            return this;
        }

        public Builder walletDeductedAmount(Money walletDeductedAmount) {
            this.walletDeductedAmount = walletDeductedAmount;
            return this;
        }


        public Builder orderItems(List<OrderItemSnapshot> orderItems) {
            this.orderItems = orderItems;
            return this;
        }

        public Builder status(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public Builder type(PaymentType type) {
            this.type = type;
            return this;
        }

        public Builder method(PaymentMethod method) {
            this.method = method;
            return this;
        }

        public Builder paymentKey(String paymentKey) {
            this.paymentKey = paymentKey;
            return this;
        }

        public Builder lastTransactionKey(String lastTransactionKey) {
            this.lastTransactionKey = lastTransactionKey;
            return this;
        }

        public Builder approveCode(String approveCode) {
            this.approveCode = approveCode;
            return this;
        }

        public Builder paidAt(LocalDateTime paidAt) {
            this.paidAt = paidAt;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Payment build() {
            validate();
            return new Payment(
                    id,
                    type, method,
                    orderId, orderNumber, memberId,
                    originAmount, paidAmount, refundedAmount, walletDeductedAmount,
                    orderItems, status, paymentKey, lastTransactionKey, approveCode,
                    paidAt, createdAt
            );
        }

        private void validate() {
            validateRequiredFields();
            validateAmountInvariant();
            validateOrderItemsIfRequired();
        }

        private void validateRequiredFields() {
            requireNonBlank(orderNumber, "orderNumber");
            requireNonNull(memberId, "memberId");
            requireNonNull(type, "type");
            requireNonNull(method, "method");
            requireNonNull(originAmount, "originAmount");
            requireNonNull(paidAmount, "paidAmount");
            requireNonNull(status, "status");
        }

        private void requireNonBlank(String value, String fieldName) {
            if (value == null || value.isBlank()) {
                throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
                        "[Payment] " + fieldName + "는 필수입니다.");
            }
        }

        private void requireNonNull(Object value, String fieldName) {
            if (value == null) {
                throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
                        "[Payment] " + fieldName + "는 필수입니다.");
            }
        }

        private void validateAmountInvariant() {
            if (paidAmount.isGreaterThan(originAmount)) {
                throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
                        "[Payment] paidAmount는 originAmount를 초과할 수 없습니다.");
            }
            Money effectiveRefundedAmount = refundedAmount != null ? refundedAmount : Money.zero();
            if (effectiveRefundedAmount.isGreaterThan(paidAmount)) {
                throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
                        "[Payment] refundedAmount는 paidAmount를 초과할 수 없습니다.");
            }
        }

        private void validateOrderItemsIfRequired() {
            if (type == PaymentType.DEPOSIT_CHARGE) {
                return;
            }

            if (orderItems == null || orderItems.isEmpty()) {
                throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
                        "[Payment] orderItems는 필수입니다.");
            }

            Money itemsTotal = orderItems.stream()
                    .map(OrderItemSnapshot::amount)
                    .reduce(Money.zero(), Money::plus);

            if (!itemsTotal.equals(originAmount)) {
                throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
                        "[Payment] orderItems 합계와 originAmount가 일치하지 않습니다.");
            }
        }
    }

    // ========== 정적 팩토리 메서드  ========== //

    public static Payment create(
            PaymentCreateContext context,
            Money originAmount,
            Money paidAmount,
            List<OrderItemSnapshot> orderItems
    ) {
        return builder()
                .orderId(context.orderId())
                .orderNumber(context.orderNumber())
                .memberId(context.memberId())
                .type(context.type())
                .method(context.method())
                .originAmount(originAmount)
                .paidAmount(paidAmount)
                .orderItems(orderItems)
                .status(PaymentStatus.PENDING)
                .build();
    }

    public static Payment create(
            PaymentCreateContext context,
            Money originAmount,
            Money paidAmount,
            Money walletDeductedAmount,
            List<OrderItemSnapshot> orderItems
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
                .orderItems(orderItems)
                .status(PaymentStatus.PENDING)
                .build();
    }

}
