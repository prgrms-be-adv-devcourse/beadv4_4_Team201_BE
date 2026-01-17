package app.giftify.support.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 변경 이력(History) 저장을 위한 JPA 베이스 엔티티.
 *
 * 생성 시점의 정보만 기록하며,
 * 생성 이후에는 수정되지 않는 이력성 엔티티에서 사용한다.
 */
@Getter
@Immutable
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseJpaHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}