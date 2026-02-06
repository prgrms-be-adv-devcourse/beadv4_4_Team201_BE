package app.giftify.settlement.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_snapshot")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OrderSnapshot {
    @Id
    private Long orderId;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Column(nullable = false)
    private LocalDateTime orderedAt;
}
