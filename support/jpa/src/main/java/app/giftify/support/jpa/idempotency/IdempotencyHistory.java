package app.giftify.support.jpa.idempotency;

import app.giftify.support.jpa.BaseJpaHistoryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "idempotency_history")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyHistory extends BaseJpaHistoryEntity {

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private String payloadHash;

    @Column(nullable = false)
    private String domainType;

    @Column
    private Long requesterId;
}
