package app.giftify.wishlist.core.domain;

import java.time.LocalDateTime;

import app.giftify.shared.domain.base.BaseDomainModel;

public class Wishlist extends BaseDomainModel {

	private final Long memberId;
	private Visibility visibility;
	private final LocalDateTime createdAt;

	private Wishlist(Long id, Long memberId, Visibility visibility) {
		super(id);
		validateMemberId(memberId);
		this.memberId = memberId;
		this.visibility = visibility;
		this.createdAt = LocalDateTime.now();
	}

	private Wishlist(Long id, Long memberId, Visibility visibility, LocalDateTime createdAt) {
		super(id);
		validateMemberId(memberId);
		this.memberId = memberId;
		this.visibility = visibility;
		this.createdAt = createdAt;
	}

	private void validateMemberId(Long memberId) {
		if (memberId == null || memberId <= 0) {
			throw new IllegalArgumentException("유효하지 않은 회원 ID입니다.");
		}
	}

	// TODO: 도메인 검증 로직 아래에 추가

	public void changeVisibility(Visibility visibility) {
		this.visibility = visibility;
	}

	public Long getMemberId() {
		return memberId;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	@Override
	public String toString() {
		return "Wishlist{" +
			"memberId=" + memberId +
			", visibility=" + visibility +
			", createdAt=" + createdAt +
			'}';
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Long id;
		private Long memberId;
		private Visibility visibility = Visibility.PUBLIC;
		private LocalDateTime createdAt;

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder memberId(Long memberId) {
			this.memberId = memberId;
			return this;
		}

		public Builder visibility(Visibility visibility) {
			if (visibility != null)
				this.visibility = visibility;
			return this;
		}

		public Builder createdAt(LocalDateTime createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Wishlist build() {
			if (id == null) {
				return new Wishlist(null, memberId, visibility); // 생성
			}
			return new Wishlist(id, memberId, visibility, createdAt); // 복원
		}
	}
}
