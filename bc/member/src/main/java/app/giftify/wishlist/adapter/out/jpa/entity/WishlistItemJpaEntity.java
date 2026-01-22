package app.giftify.wishlist.adapter.out.jpa.entity;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import app.giftify.support.jpa.BaseJpaEntity;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
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
@Table(name = "wishlist_item")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class WishlistItemJpaEntity extends BaseJpaEntity {

	@Column(name = "wishlist_id", nullable = false)
	private Long wishlistId;

	@Column(nullable = false)
	private Long productId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private WishlistItemStatus wishlistItemStatus;

	private LocalDate addedAt;
}
