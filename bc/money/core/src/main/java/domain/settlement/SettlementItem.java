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
    private Money settlementAmount;

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

        // todo: 상태 및 타입에 따른 도메인 검증

        if (status == SettlementItemStatus.PENDING) {
            if (paymentInfo != null || feeInfo != null || expectedDate != null) {
                throw new IllegalStateException("결제 대기 상태에서는 결제 및 수수료 정보가 존재할 수 없습니다.");
            }
            if (settlementId != null) {
                throw new IllegalStateException("결제 대기 상태에서는 정산 그룹 ID가 존재할 수 없습니다.");
            }
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
        this.settlementAmount = calculateAmount();
    }

    private Money calculateAmount() {
        if (feeInfo == null) return Money.zero();
        return getTotalAmount().minus(feeInfo.platformFee()).minus(feeInfo.pgFee());
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
        return settlementAmount != null ? settlementAmount : Money.zero();
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
        validateNewOrder(sellerId, orderItemInfo);

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
}
