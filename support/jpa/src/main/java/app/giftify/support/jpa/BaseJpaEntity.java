package app.giftify.support.jpa;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import app.giftify.shared.domain.event.BaseAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * JPA 엔티티들의 공통 기반 클래스.
 * 모든 엔티티는 이 클래스를 상속받아 공통 필드(ID, 생성/수정 시간 및 사용자)를 관리한다.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseJpaEntity extends BaseAggregateRoot {

	/**
	 * 기본 키 (ID).
	 * 데이터베이스의 IDENTITY 전략(Auto Increment)을 기본으로 사용한다.
	 * 엔티티마다 다른 전략(예: UUID)이 필요한 경우, 이 필드를 사용하지 않고 해당 엔티티에서 직접 정의하는 것을 권장한다.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	/**
	 * 생성자 ID.
	 * JPA Auditing 설정에 따라 현재 로그인한 사용자의 정보가 자동으로 채워진다.
	 */
	@CreatedBy
	@Column(name = "created_by", updatable = false)
	private String createdBy;

	/**
	 * 수정자 ID.
	 */
	@LastModifiedBy
	@Column(name = "updated_by")
	private String updatedBy;

	// === 생성자 ===

	protected BaseJpaEntity() {
	}

	protected BaseJpaEntity(Long id) {
		this.id = id;
	}
}
