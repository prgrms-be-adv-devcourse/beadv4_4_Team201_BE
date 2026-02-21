package app.giftify.orderDemo.domain;

import app.giftify.orderDemo.domain.errorCode.OrderErrorCode;
import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.shared.domain.event.BaseAggregateRoot;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.vo.Money;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "order_v2")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
@EntityListeners(AuditingEntityListener.class)
@ToString
public class Order extends BaseAggregateRoot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Long quantity;

    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false, precision = 19, scale = 2)
    private Money totalAmount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<OrderItem> items = new ArrayList<>();

    @Column(unique = true)
    private Long paymentId;

    @Column
    private String originTransactionKey;

    @Column
    private LocalDateTime paidAt;

    @Column
    private LocalDateTime cancelRequestedAt;

    @Column
    private LocalDateTime cancelledAt;

    @Column
    private LocalDateTime confirmedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static Order create(
            Long buyerId,
            List<OrderItem> items,
            PaymentMethod paymentMethod
    ) {
        if (buyerId == null) throw new DomainException(OrderErrorCode.INVALID_BUYER_ID);
        if (items == null || items.isEmpty()) throw new DomainException(OrderErrorCode.INVALID_ORDER_ITEM);
        if (paymentMethod == null) throw new DomainException(OrderErrorCode.INVALID_PAYMENT_METHOD);

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .buyerId(buyerId)
                .paymentMethod(paymentMethod)
                .status(OrderStatus.CREATED)
                .build();

        items.forEach(order::addItem);
        order.setTotalAmount();
        order.setQuantity(items.size());

        if (order.getTotalAmount().isLessThan(Money.of(1000L)))
            throw new DomainException(OrderErrorCode.INVALID_TOTAL_AMOUNT);

        return order;
    }

    public OrderSnapshot toSnapshot() {
        List<OrderItemSnapshot> itemSnapshots = items.stream()
                .map(OrderItem::toSnapshot)
                .toList();

        return OrderSnapshot.builder()
                .orderId(id)
                .orderNumber(orderNumber)
                .buyerId(buyerId)
                .orderItemSnapshots(itemSnapshots)
                .totalAmount(totalAmount)
                .paymentMethod(paymentMethod)
                .status(status)
                .createdAt(createdAt)
                .build();
    }

    public void paid(Long paymentId, String originTransactionKey) {
        if (status != OrderStatus.CREATED) {
            throw new PolicyException(
                    OrderErrorCode.INVALID_STATUS_TRANSITION,
                    String.format("주문 결제 완료는 생성 상태에서만 가능합니다. (현재: %s)", status)
            );
        }

        this.paymentId = paymentId;
        this.originTransactionKey = originTransactionKey;
        this.paidAt = LocalDateTime.now();
        this.status = OrderStatus.PAID;

        items.forEach(i -> i.paid(originTransactionKey));
    }

    public void synchronizeStatus() {
        this.status = OrderStatus.deriveStatus(getItemStatuses());

        if (status == OrderStatus.CANCELING){
            cancelRequestedAt = LocalDateTime.now();
        } else if (status == OrderStatus.CANCELED) {
            cancelledAt = LocalDateTime.now();
        }
    }

    public void cancelAll() {
        validateFullCancelable();

        if (this.status == OrderStatus.CREATED) {
            this.items.forEach(OrderItem::canceled);
            canceled();
        }

        if (this.status == OrderStatus.PAID) {
            this.items.forEach(OrderItem::canceling);
            canceling();;
        }

        throw new PolicyException(OrderErrorCode.INVALID_STATUS_TRANSITION);
    }

    public List<OrderItem> getCancelingItems() {
        return items.stream()
                .filter(OrderItem::isCanceling)
                .toList();
    }

    private static String generateOrderNumber() {
        return "ORD-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase()
                + "-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    private void setTotalAmount() {
        this.totalAmount = items.stream()
                .map(OrderItem::getAmount)
                .reduce(Money.zero(), Money::plus);
    }

    private void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    private boolean isAllItemsFullCancelable() {
        OrderItem firstItem = items.get(0);
        OrderItemStatus firstStatus = firstItem.getStatus();

        if (!firstItem.isCancelable())
            return false;

        return items.stream()
                .allMatch(item -> item.getStatus() == firstStatus);
    }

    private void canceling() {
        this.status = OrderStatus.CANCELING;
        this.cancelRequestedAt = LocalDateTime.now();
    }

    private void canceled() {
        this.status = OrderStatus.CANCELED;
        this.cancelledAt = LocalDateTime.now();
    }

    private void validateFullCancelable() {
        if (items.isEmpty()) {
            throw new DomainException(
                    OrderErrorCode.ORDER_ITEM_NOT_FOUND,
                    String.format("주문 취소 항목이 존재하지 않습니다. orderId = %d", id)
            );
        }

        if (!status.isFullCancelable()) {
            throw new PolicyException(
                    OrderErrorCode.INVALID_STATUS_TRANSITION,
                    String.format("주문 전체 취소가 불가능한 주문 상태입니다. orderId = %d, status = %s", id, status)
            );
        }

        if (!isAllItemsFullCancelable()) {
            throw new PolicyException(
                    OrderErrorCode.INVALID_STATUS_TRANSITION,
                    String.format("주문 전체 취소가 불가능한 항목이 포함되어 있거나 상태가 일치하지 않습니다. orderId = %d", id)
            );
        }
    }

    public List<OrderItem> validatePartialCancelable(List<Long> requestedItemIds) {
        List<OrderItem> targets = items.stream()
                .filter(item -> requestedItemIds.contains(item.getId()))
                .toList();

        if (targets.isEmpty()) {
            throw new DomainException(
                    OrderErrorCode.ORDER_ITEM_NOT_FOUND,
                    String.format("주문 취소 항목이 존재하지 않습니다. orderId = %d", id)
            );
        }

        if (targets.size() != requestedItemIds.size()) {
            throw new DomainException(
                    OrderErrorCode.INVALID_ORDER_ITEM,
                    String.format("주문 부분 취소 항목 중 주문에 존재하지 않은 항목이 존재합니다. orderId = %d", id)
            );
        }

        boolean allCancelable = targets.stream().allMatch(OrderItem::isCancelable);

        if (!allCancelable) {
            throw new PolicyException(
                    OrderErrorCode.INVALID_STATUS_TRANSITION,
                    String.format("주문 부분 취소가 불가능한 주문 항목이 포함되어 있습니다. orderId = %d", id)
            );
        }

        return targets;
    }

    private List<OrderItemStatus> getItemStatuses() {
        return items.stream()
                .map(OrderItem::getStatus)
                .toList();
    }
}
