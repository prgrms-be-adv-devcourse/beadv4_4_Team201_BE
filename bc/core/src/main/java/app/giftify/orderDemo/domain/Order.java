package app.giftify.orderDemo.domain;

import app.giftify.order.domain.OrderStatus;
import app.giftify.shared.domain.type.PaymentMethodType;
import app.giftify.shared.domain.vo.Money;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "order")
@NoArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Order {
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
    private PaymentMethodType paymentMethod;

    @Column(nullable = false)
    private OrderStatus status;

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

    @Builder
    public Order(String orderNumber,
                 Long buyerId,
                 Money totalAmount,
                 OrderStatus status,
                 PaymentMethodType paymentMethod) {
        this.orderNumber = orderNumber;
        this.buyerId = buyerId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }
}
