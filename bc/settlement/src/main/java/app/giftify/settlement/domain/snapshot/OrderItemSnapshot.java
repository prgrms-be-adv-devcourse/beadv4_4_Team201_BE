package app.giftify.settlement.domain.snapshot;

import app.giftify.settlement.domain.support.MoneyConverter;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_item_snapshot")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OrderItemSnapshot {
    @Id
    private Long orderItemId;

    @Column
    private Long orderId;

    @Column(nullable = false, unique = true)
    private Long targetId;

    @Column
    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    @Column
    @Enumerated(EnumType.STRING)
    private OrderItemType orderItemType;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    @Convert(converter = MoneyConverter.class)
    private Money price;

    @Column(nullable = false)
    @Convert(converter = MoneyConverter.class)
    private Money amount;
}
