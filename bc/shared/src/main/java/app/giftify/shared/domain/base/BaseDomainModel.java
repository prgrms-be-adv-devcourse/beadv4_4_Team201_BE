package app.giftify.shared.domain.base;

import java.time.LocalDateTime;

/**
 * 모든 도메인 모델의 최상위 부모 클래스
 */
public abstract class BaseDomainModel {
    private final Long id;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    protected BaseDomainModel(Long id, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
