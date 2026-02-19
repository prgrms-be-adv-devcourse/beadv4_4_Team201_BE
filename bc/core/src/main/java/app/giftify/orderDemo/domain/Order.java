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
    private String paymentKey;

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

    public void toPaid(String paymentKey, String originTransactionKey) {
        if (this.status == OrderStatus.PAID) {
            return;
        }

        if (status != OrderStatus.CREATED) {
            throw new PolicyException(
                    OrderErrorCode.INVALID_STATUS_TRANSITION,
                    String.format("주문 결제 완료는 생성 상태에서만 가능합니다. (현재: %s)", status)
            );
        }

        this.paymentKey = paymentKey;
        this.originTransactionKey = originTransactionKey;
        this.paidAt = LocalDateTime.now();
        this.status = OrderStatus.PAID;

        items.forEach(i -> i.toPaid(originTransactionKey));
    }

    public void cancel() {
        if (status == OrderStatus.CANCELED) {
            throw new PolicyException(
                    OrderErrorCode.ALREADY_CANCELED,
                    String.format("이미 취소된 주문입니다. orderId = %s", id)
            );
        }
    }

    public void pendingToCancel(LocalDateTime cancelRequestedAt) {
        if (status == OrderStatus.CANCELED || status == OrderStatus.CANCEL_PENDING) {
            throw new PolicyException(
                    OrderErrorCode.ALREADY_CANCELED,
                    String.format("이미 취소(요청)된 주문입니다. orderId = %s", id)
            );
        }
        status = OrderStatus.CANCEL_PENDING;
        this.cancelRequestedAt = cancelRequestedAt;
    }
}
