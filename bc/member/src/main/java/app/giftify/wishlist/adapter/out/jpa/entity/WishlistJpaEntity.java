package app.giftify.wishlist.adapter.out.jpa.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import app.giftify.support.jpa.BaseJpaEntity;
import app.giftify.wishlist.core.domain.Visibility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wishlist")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class WishlistJpaEntity extends BaseJpaEntity {

	// @Column(nullable = false, unique = true)
	// private String authSub;

	@Column(nullable = false)
	private Long memberId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Visibility visibility;
}
