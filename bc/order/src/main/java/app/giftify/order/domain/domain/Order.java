package app.giftify.order.domain.domain;

import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.vo.Money;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Order extends BaseDomainModel {

    private final String orderNumber;
    private final Long buyerId;
    private final LocalDateTime createdAt;

    private Money totalAmount;
    private OrderStatus status;
    private String paymentKey;

    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private final List<OrderItem> orderItems;

    protected Order(
            Long id,
            String orderNumber,
            Long buyerId,
            Money totalAmount,
            OrderStatus status,
            LocalDateTime createdAt,
            String paymentKey,
            LocalDateTime confirmedAt,
            LocalDateTime canceledAt,
            List<OrderItem> orderItems
    ) {
        super(id);
        this.orderNumber = orderNumber;
        this.buyerId = buyerId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.paymentKey = paymentKey;
        this.confirmedAt = confirmedAt;
        this.cancelledAt = canceledAt;
        this.orderItems = orderItems != null ? new ArrayList<>(orderItems) : new ArrayList<>();
        validate();
    }

    // 주문 유효성 검사
    // 1. 최소 결제 금액 (1000원) 확인
    // 2. 주문 번호 길이 확인 (6~64자)
    private void validate() {
        if (totalAmount != null) {
            totalAmount.validateMinimumAmount();
        }
        // 영문 대소문자, 숫자, 특수문자 -, _로 이루어진 6자 이상 64자 이하의 문자열 확인
        if (orderNumber == null || !orderNumber.matches("^[a-zA-Z0-9-_]{6,64}$")) {
            throw new IllegalArgumentException("주문 번호는 영문 대소문자, 숫자, -, _로 구성된 6자 이상 64자 이하의 문자열이어야 합니다.");
        }
    }

    // 주문 번호 자동 생성
    // [ ORD - UUID(하이픈 제거 + upper) 앞 12자리 - 연월일시분초 ] 형식의 주문 번호 생성
    public static String generateOrderNumber() {
        return "ORD-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase()
                +"-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    // [하향식 전이] 결제 완료 처리
    // 주문 상태를 ORDERED로 변경하고 하위 모든 아이템도 ORDERED로 변경
    public void toOrdered(String paymentKey) {
        if (this.status != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("주문 대기 상태에서만 결제가 가능합니다.");
        }
        this.status = OrderStatus.ORDERED;
        this.paymentKey = paymentKey;
        this.orderItems.forEach(OrderItem::toOrdered);
    }

    // [하향식 전이] 전체 주문 확정 처리
    // 주문 상태를 CONFIRMED로 변경하고 하위 모든 아이템도 CONFIRMED로 변경
    public void toConfirmed() {
        if (this.status != OrderStatus.ORDERED) {
            throw new IllegalStateException("주문 결제 완료 상태에서만 확정이 가능합니다.");
        }
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.orderItems.forEach(OrderItem::toConfirmed);
    }

    // [하향식 전이] 주문 취소 처리
    // 이미 확정된(CONFIRMED) 주문은 취소 불가
    // 주문 상태를 CANCELED로 변경하고 하위 모든 아이템도 CANCELED로 변경
    public void toCancelled() {
        if (this.status == OrderStatus.CONFIRMED) {
            throw new IllegalStateException("이미 확정된 주문은 취소할 수 없습니다.");
        }
        this.status = OrderStatus.CANCELED;
        this.cancelledAt = LocalDateTime.now();
        this.orderItems.forEach(OrderItem::toCancelled);
    }

    // 자동 취소 가능 여부 판단
    // PAYMENT_PENDING 상태이며, 생성 후 특정 시간이 경과했는지 확인
    public boolean canAutoCancel(int minutes) {
        return this.status == OrderStatus.PAYMENT_PENDING
                && this.createdAt.plusMinutes(minutes).isBefore(LocalDateTime.now());
    }

    // [상향식 전이] 모든 아이템 확정 여부 확인 및 주문 상태 업데이트
    // 모든 OrderItem의 상태가 CONFIRMED가 되면, Order의 상태도 CONFIRMED로 변경
    public void checkAllItemsConfirmed() {
        boolean allConfirmed = !orderItems.isEmpty() && orderItems.stream()
                .allMatch(item -> item.getStatus() == OrderStatus.CONFIRMED);

        if (allConfirmed) {
            this.status = OrderStatus.CONFIRMED;
            this.confirmedAt = LocalDateTime.now();
        }
    }

    // [상향식 전이] 모든 아이템 취소 여부 확인 및 주문 상태 업데이트
    // 모든 OrderItem의 상태가 CANCELED가 되면, Order의 상태도 CANCELED로 변경
    public void checkAllItemsCancelled() {
        boolean allCancelled = !orderItems.isEmpty()
                && orderItems.stream().allMatch(item -> item.getStatus() == OrderStatus.CANCELED);

        if (allCancelled) {
            this.status = OrderStatus.CANCELED;
            this.cancelledAt = LocalDateTime.now();
        }
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public Money getTotalAmount() {
        return totalAmount;
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

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String orderNumber;
        private Long buyerId;
        private Money totalAmount;
        private OrderStatus status;
        private LocalDateTime createdAt;
        private String paymentKey;
        private LocalDateTime confirmedAt;
        private LocalDateTime canceledAt;
        private List<OrderItem> orderItems = new ArrayList<>();

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

        public Builder cancelledAt(LocalDateTime canceledAt) {
            this.canceledAt = canceledAt;
            return this;
        }

        public Builder orderItems(List<OrderItem> orderItems) {
            this.orderItems = orderItems != null ? new ArrayList<>(orderItems) : new ArrayList<>();
            return this;
        }

        public Order build() {
            return new Order(
                    id,
                    orderNumber,
                    buyerId,
                    totalAmount,
                    status,
                    createdAt,
                    paymentKey,
                    confirmedAt,
                    canceledAt,
                    orderItems
            );
        }
    }
}