package app.giftify.support.common.base;

import app.giftify.support.common.base.BaseAggregateRoot;

import java.util.Objects;

/**
 * 모든 도메인 모델의 최상위 부모 클래스
 */
public abstract class BaseDomainModel extends BaseAggregateRoot {
    private final Long id;

    protected BaseDomainModel(Long id) {
        this.id = id;
    }

    public String getModelType() {
        return this.getClass().getSimpleName();
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass())
            return false;

        BaseDomainModel that = (BaseDomainModel) object;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
