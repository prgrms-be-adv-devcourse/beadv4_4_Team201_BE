package domain.settlement;

import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.type.PaymentMethodType;
import app.giftify.shared.domain.vo.FeeInfo;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.OrderItemInfo;
import app.giftify.shared.domain.vo.PaymentInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SettlementItem extends BaseDomainModel {
    private Long sellerId;
    private Long settlementId;

    private OrderItemInfo orderItemInfo;
    private PaymentInfo paymentInfo;
    private FeeInfo feeInfo;

    private Long originId;
    private SettlementItemType type;

    private SettlementItemStatus status;
    private LocalDate expectedDate;

    private SettlementItem(
            Long id,
            Long sellerId,
            Long settlementId,
            OrderItemInfo orderItemInfo,
            PaymentInfo paymentInfo,
            FeeInfo feeInfo,
            Long originId,
            SettlementItemType type,
            SettlementItemStatus status,
            LocalDate expectedDate
    ) {
        super(id);

        if (status == SettlementItemStatus.COMPLETED && settlementId == null) {
            throw new IllegalStateException("정산 완료 상태에는 settlementId가 필요합니다.");
        }

        if (status == SettlementItemStatus.READY && settlementId != null) {
            throw new IllegalStateException("정산 전에는 settlementId가 존재할 수 없습니다.");
        }

        if (type == SettlementItemType.DEDUCTION_REFUND && originId == null) {
            throw new IllegalStateException("정산 환불인 경우 originId가 필요합니다.");
        }

        this.sellerId = sellerId;
        this.settlementId = settlementId;
        this.orderItemInfo = orderItemInfo;
        this.paymentInfo = paymentInfo;
        this.feeInfo = feeInfo;
        this.originId = originId;
        this.type = type;
        this.status = status;
        this.expectedDate = expectedDate;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public OrderItemInfo getOrderInfo() {
        return orderItemInfo;
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
        return paymentInfo;
    }

    public String getPaymentKey() {
        return paymentInfo.paymentKey();
    }

    public String getTransactionKey() {
        return paymentInfo.transactionKey();
    }

    public PaymentMethodType getPaymentMethodType() {
        return paymentInfo.paymentMethodType();
    }

    public FeeInfo getFeeInfo() {
        return feeInfo;
    }

    public Money getPlatformFee() {
        return feeInfo.platformFee();
    }

    public Money getPgFee() {
        return feeInfo.pgFee();
    }

    public Money getSettlementAmount() {
        return feeInfo.getSettlementAmount(getTotalAmount());
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
        return settlementId;
    }

    public static SettlementItem create(Long sellerId, OrderItemInfo orderItemInfo) {
        return new SettlementItem(
                null,
                sellerId,
                null,
                orderItemInfo,
                null,
                null,
                null,
                SettlementItemType.ITEM_PAYMENT,
                SettlementItemStatus.PENDING,
                null
        );
    }
}
