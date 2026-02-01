package app.giftify.orderDemo.domain;

import app.giftify.orderDemo.domain.errorCode.OrderErrorCode;
import app.giftify.orderDemo.domain.exception.DomainException;
import app.giftify.shared.domain.event.BaseAggregateRoot;
import app.giftify.shared.domain.type.PaymentMethodType;
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
@Table(name = "order")
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

    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false, precision = 19, scale = 2)
    private Money totalAmount;

    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private PaymentMethodType paymentMethod;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<OrderItem> items = new ArrayList<>();

    @Column(unique = true)
    private String paymentKey;

    @Column
    private String lastTransactionKey;

    @Column
    private LocalDateTime paidAt;

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
            PaymentMethodType paymentMethod
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

    public OrderSnapshot toSnapshot() {
        List<OrderItemSnapshot> itemSnapshots = items.stream()
                .map(OrderItem::toSnapshot)
                .toList();

        return OrderSnapshot.builder()
                .orderNumber(orderNumber)
                .buyerId(buyerId)
                .paymentMethod(paymentMethod)
                .status(status)
                .totalAmount(totalAmount)
                .createdAt(createdAt)
                .orderItemSnapshots(itemSnapshots)
                .build();
    }
}
