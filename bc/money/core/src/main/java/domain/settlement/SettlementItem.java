package domain.settlement;

import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.type.PaymentMethodType;
import app.giftify.shared.domain.vo.AmountInfo;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.OrderItemInfo;
import app.giftify.shared.domain.vo.PaymentInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SettlementItem extends BaseDomainModel {
    private Long sellerId;

    private OrderItemInfo orderItemInfo;
    private PaymentInfo paymentInfo;
    private AmountInfo amountInfo;

    private Long originId;
    private SettlementItemType type;
    private SettlementItemStatus status;
    private LocalDateTime occurredAt;
    private LocalDate expectedDate;

    private Long settlementId;
    private LocalDateTime settledAt;
    private LocalDateTime cancelledAt;

    private SettlementItem(
            Long id, Long sellerId, OrderItemInfo orderItemInfo,
            PaymentInfo paymentInfo, AmountInfo amountInfo, Long originId,
            SettlementItemType type, SettlementItemStatus status, LocalDateTime occurredAt,
            LocalDate expectedDate, Long settlementId, LocalDateTime settledAt, LocalDateTime cancelledAt
    ) {
        super(id);
        validateState(status, paymentInfo, amountInfo, expectedDate, settlementId, settledAt, cancelledAt);

        this.sellerId = sellerId;
        this.orderItemInfo = orderItemInfo;
        this.paymentInfo = paymentInfo;
        this.amountInfo = amountInfo;
        this.originId = originId;
        this.type = type;
        this.status = status;
        this.occurredAt = occurredAt != null ? occurredAt : LocalDateTime.now();
        this.expectedDate = expectedDate;
        this.settlementId = settlementId;
        this.settledAt = settledAt;
        this.cancelledAt = cancelledAt;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public Long getOrderId() {
        return orderItemInfo.orderId();
    }

    public String getOrderNumber() {
        return orderItemInfo.orderNumber();
    }

    public Long getOrderItemId() {
        return orderItemInfo.orderItemId();
    }

    public Long getQuantity() {
        return orderItemInfo.quantity();
    }

    public Money getTotalAmount() {
        return orderItemInfo.totalAmount();
    }

    public LocalDateTime getOrderedAt() {
        return orderItemInfo.orderedAt();
    }

    public PaymentInfo getPaymentInfo() {
        validatePaymentInfoAvailability();

        return paymentInfo;
    }

    public String getPaymentKey() {
        validatePaymentInfoAvailability();

        return paymentInfo.paymentKey();
    }

    public String getTransactionKey() {
        validatePaymentInfoAvailability();

        return paymentInfo.transactionKey();
    }

    public PaymentMethodType getPaymentMethodType() {
        validatePaymentInfoAvailability();

        return paymentInfo.paymentMethodType();
    }

    public LocalDateTime getPaidAt() {
        validatePaymentInfoAvailability();

        return paymentInfo.paidAt();
    }

    public Money getPlatformFee() {
        validateAmountInfoAvailability();

        return amountInfo.platformFee();
    }

    public Money getPgFee() {
        validateAmountInfoAvailability();

        return amountInfo.pgFee();
    }

    public Money getSettlementAmount() {
        validateAmountInfoAvailability();

        return amountInfo.settlementAmount();
    }

    public SettlementItemType getType() {
        return type;
    }

    public Long getOriginId() {
        return originId;
    }

    public SettlementItemStatus getStatus() {
        return status;
    }

    public LocalDate getExpectedDate() {
        return expectedDate;
    }

    public Long getSettlementId() {
        validateSettlementAvailability();

        return settlementId;
    }

    public OrderItemInfo getOrderItemInfo() {
        return orderItemInfo;
    }

    public AmountInfo getAmountInfo() {
        validateAmountInfoAvailability();

        return amountInfo;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public LocalDateTime getSettledAt() {
        validateSettlementAvailability();

        return settledAt;
    }

    public LocalDateTime getCancelledAt() {
        if (this.status != SettlementItemStatus.CANCELLED) {
            throw new IllegalStateException("결제 취소인 경우에만 취소 정보를 조회할 수 있습니다.");
        }
        return cancelledAt;
    }

    public static SettlementItem createPaymentItem(Long sellerId, OrderItemInfo orderItemInfo) {
        validateNewOrder(sellerId, orderItemInfo);

        return new SettlementItem(
                null,
                sellerId,
                orderItemInfo,
                null,
                null,
                null,
                SettlementItemType.ITEM_PAYMENT,
                SettlementItemStatus.PENDING,
                LocalDateTime.now(),
                null,
                null,
                null,
                null
        );
    }

    private static void validateNewOrder(Long sellerId, OrderItemInfo orderItemInfo) {
        if (sellerId == null) {
            throw new IllegalArgumentException("판매자 ID는 필수입니다.");
        }
        if (orderItemInfo == null) {
            throw new IllegalArgumentException("주문 정보는 필수입니다.");
        }
        if (orderItemInfo.totalAmount().isLessThanOrEqual(Money.zero())) {
            throw new IllegalArgumentException("정산 대상 금액은 0원보다 커야 합니다.");
        }
        if (orderItemInfo.quantity() < 1) {
            throw new IllegalArgumentException("주문 수량은 1개 이상이어야 합니다.");
        }
    }

    private void validateState(SettlementItemStatus status, PaymentInfo paymentInfo, AmountInfo amountInfo, LocalDate expectedDate, Long settlementId, LocalDateTime settledAt, LocalDateTime cancelledAt) {
        if (status == SettlementItemStatus.PENDING) {
            if (paymentInfo != null || amountInfo != null || expectedDate != null || settlementId != null || settledAt != null || cancelledAt != null) {
                throw new IllegalStateException("결제 대기 상태에서는 결제/금액/정산/취소 정보가 존재할 수 없습니다.");
            }
        }
    }

    private void validatePaymentInfoAvailability() {
        if (this.status == SettlementItemStatus.PENDING) {
            throw new IllegalStateException("결제 대기 상태에서는 결제 정보를 조회할 수 없습니다.");
        }
    }

    private void validateAmountInfoAvailability() {
        if (this.status == SettlementItemStatus.PENDING) {
            throw new IllegalStateException("결제 대기 상태에서는 금액 정보를 조회할 수 없습니다.");
        }

        if (this.status == SettlementItemStatus.CANCELLED && this.amountInfo == null) {
            throw new IllegalStateException("결제 전 취소된 항목은 금액 정보가 존재하지 않습니다.");
        }
    }

    private void validateSettlementAvailability() {
        if (this.status != SettlementItemStatus.COMPLETED) {
            throw new IllegalStateException("정산 완료 상태인 경우에만 정산 정보를 조회할 수 있습니다.");
        }
    }
}
