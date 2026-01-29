package app.giftify.settlement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "funding_snapshot")
@NoArgsConstructor
@AllArgsConstructor
public class FundingSnapshot {
    @Id
    private Long fundingId;

    @Column(nullable = false, unique = true)
    private Long orderItemId;

    @Column(nullable = false)
    private LocalDateTime confirmedAt;
}
