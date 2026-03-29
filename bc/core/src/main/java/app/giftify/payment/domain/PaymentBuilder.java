package app.giftify.payment.domain;

import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

import java.time.LocalDateTime;
import java.util.List;

public class PaymentBuilder {
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

    public PaymentBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public PaymentBuilder orderId(Long orderId) {
        this.orderId = orderId;
        return this;
    }

    public PaymentBuilder orderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
        return this;
    }

    public PaymentBuilder memberId(Long memberId) {
        this.memberId = memberId;
        return this;
    }

    public PaymentBuilder originAmount(Money originAmount) {
        this.originAmount = originAmount;
        return this;
    }

    public PaymentBuilder paidAmount(Money paidAmount) {
        this.paidAmount = paidAmount;
        return this;
    }

    public PaymentBuilder refundedAmount(Money refundedAmount) {
        this.refundedAmount = refundedAmount;
        return this;
    }

    public PaymentBuilder walletDeductedAmount(Money walletDeductedAmount) {
        this.walletDeductedAmount = walletDeductedAmount;
        return this;
    }

    public PaymentBuilder orderItems(List<OrderItemSnapshot> orderItems) {
        this.orderItems = orderItems;
        return this;
    }

    public PaymentBuilder status(PaymentStatus status) {
        this.status = status;
        return this;
    }

    public PaymentBuilder type(PaymentType type) {
        this.type = type;
        return this;
    }

    public PaymentBuilder method(PaymentMethod method) {
        this.method = method;
        return this;
    }

    public PaymentBuilder paymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
        return this;
    }

    public PaymentBuilder lastTransactionKey(String lastTransactionKey) {
        this.lastTransactionKey = lastTransactionKey;
        return this;
    }

    public PaymentBuilder approveCode(String approveCode) {
        this.approveCode = approveCode;
        return this;
    }

    public PaymentBuilder paidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
        return this;
    }

    public PaymentBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Payment build() {
        validate();
        return Payment.fromBuilder(
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
