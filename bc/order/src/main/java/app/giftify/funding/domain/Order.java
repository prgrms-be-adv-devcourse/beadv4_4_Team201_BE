package app.giftify.funding.domain;

import app.giftify.funding.domain.exception.OrderErrorCode;
import app.giftify.funding.domain.exception.OrderException;
import app.giftify.funding.domain.vo.Money;
import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.type.PaymentMethod;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Order extends BaseDomainModel {

    private final String orderNumber;
    private final Long buyerId;
    private final Money totalAmount;
    private final PaymentMethod paymentMethod;
    private final LocalDateTime createdAt;

    private String paymentKey;
    private OrderStatus status;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;

    protected Order(
            Long id,
            String orderNumber,
            Long buyerId,
            Money totalAmount,
            PaymentMethod paymentMethod,
            OrderStatus status,
            LocalDateTime createdAt,
            String paymentKey,
            LocalDateTime confirmedAt,
            LocalDateTime cancelledAt
    ) {
        super(id);
        this.orderNumber = orderNumber;
        this.buyerId = buyerId;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = createdAt;
        this.paymentKey = paymentKey;
        this.confirmedAt = confirmedAt;
        this.cancelledAt = cancelledAt;
        validate();
    }

    // 주문 유효성 검사
    private void validate() {
        // 주문 금액 최소값(1000) 검증은 Money 생성자에 추가

        if (paymentMethod == null) {
            throw new OrderException(OrderErrorCode.INVALID_INPUT_VALUE, "결제 수단은 필수입니다.");
        }

        if (orderNumber == null || !orderNumber.matches("^[a-zA-Z0-9-_]{6,64}$")) {
            throw new OrderException(
                    OrderErrorCode.INVALID_INPUT_VALUE,
                    "주문 번호는 영문 대소문자, 숫자, -, _로 구성된 6자 이상 64자 이하의 문자열이어야 합니다."
            );
        }
    }

    // 주문 번호 자동 생성
    public static String generateOrderNumber() {
        return "ORD-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase()
                + "-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    // 결제 완료 처리
    public void toOrdered(String paymentKey) {
        if (this.status != OrderStatus.PAYMENT_PENDING) {
            throw new OrderException(OrderErrorCode.ORDER_NOT_PAYABLE, "주문 대기 상태에서만 결제가 가능합니다.");
        }
        this.status = OrderStatus.PAID;
        this.paymentKey = paymentKey;
    }

    // 주문 확정 처리
    public void toConfirmed() {
        if (this.status != OrderStatus.PAID) {
            throw new OrderException(OrderErrorCode.ORDER_NOT_CONFIRMABLE, "주문 결제 완료 상태에서만 확정이 가능합니다.");
        }
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    // 주문 취소 처리
    public void toCancelled() {
        if (this.status == OrderStatus.CONFIRMED) {
            throw new OrderException(OrderErrorCode.ORDER_ALREADY_CONFIRMED, "이미 확정된 주문은 취소할 수 없습니다.");
        }
        this.status = OrderStatus.CANCELED;
        this.cancelledAt = LocalDateTime.now();
    }

    // 결제 재시도용 취소 처리
    public void toFailed() {
        if (this.status == OrderStatus.CONFIRMED) {
            throw new OrderException(OrderErrorCode.ORDER_ALREADY_CONFIRMED, "이미 확정된 주문은 취소할 수 없습니다.");
        }
        this.status = OrderStatus.FAILED;
    }

    // 자동 취소 가능 여부
    public boolean canAutoCancel(int minutes) {
        return this.status == OrderStatus.PAYMENT_PENDING
                && this.createdAt.plusMinutes(minutes).isBefore(LocalDateTime.now());
    }

    // 환불 처리
    public void toRefunded() {
        if(this.status == OrderStatus.CONFIRMED){
            throw new OrderException(OrderErrorCode.ORDER_ALREADY_CONFIRMED, "이미 확정된 주문은 환불할 수 없습니다.");
        }

        if(this.status != OrderStatus.PAID){
            throw new OrderException(OrderErrorCode.ORDER_CANNOT_REFUND, "결제 이력이 없어 환불 가능한 상태가 아닙니다.");
        }

        this.status = OrderStatus.REFUNDED;
    }

    // getters
    public String getOrderNumber() {
        return orderNumber;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    // builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String orderNumber;
        private Long buyerId;
        private Money totalAmount;
        private PaymentMethod paymentMethod;
        private OrderStatus status;
        private LocalDateTime createdAt;
        private String paymentKey;
        private LocalDateTime confirmedAt;
        private LocalDateTime cancelledAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder orderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
            return this;
        }

        public Builder buyerId(Long buyerId) {
            this.buyerId = buyerId;
            return this;
        }

        public Builder totalAmount(Money totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder paymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder paymentKey(String paymentKey) {
            this.paymentKey = paymentKey;
            return this;
        }

        public Builder confirmedAt(LocalDateTime confirmedAt) {
            this.confirmedAt = confirmedAt;
            return this;
        }

        public Builder cancelledAt(LocalDateTime cancelledAt) {
            this.cancelledAt = cancelledAt;
            return this;
        }

        public Order build() {
            return new Order(
                    id,
                    orderNumber,
                    buyerId,
                    totalAmount,
                    paymentMethod,
                    status,
                    createdAt,
                    paymentKey,
                    confirmedAt,
                    cancelledAt
            );
        }
    }
}