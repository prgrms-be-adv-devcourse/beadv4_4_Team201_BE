package app.giftify.wishlist.core.domain;

import java.time.LocalDate;

import app.giftify.shared.domain.base.BaseDomainModel;

public class Wishlist extends BaseDomainModel {

	private final Long memberId;
	private Visibility visibility;
	private final LocalDate createdAt;

	private Wishlist(Long id, Long memberId, Visibility visibility) {
		super(id);
		validateMemberId(memberId);
		this.memberId = memberId;
		this.visibility = visibility;
		this.createdAt = LocalDate.now();
	}

	private Wishlist(Long id, Long memberId, Visibility visibility, LocalDate createdAt) {
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

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	// public String getAuthSub() {
	//     return authSub;
	// }

	@Override
	public String toString() {
		return "Wishlist{" +
			"memberId=" + memberId +
			", visibility=" + visibility +
			", createdAt=" + createdAt +
			// ", authSub='" + authSub + '\'' +
			'}';
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Long id;
		private Long memberId;
		private Visibility visibility;
		private LocalDate createdAt;
		private String authSub;

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder memberId(Long memberId) {
			this.memberId = memberId;
			return this;
		}

		public Builder visibility(Visibility visibility) {
			this.visibility = visibility;
			return this;
		}

		public Builder createdAt(LocalDate createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Builder authSub(String authSub) {
			this.authSub = authSub;
			return this;
		}

		public Wishlist build() {
			if (createdAt == null) {
				return new Wishlist(id, memberId, visibility);
			}
			return new Wishlist(id, memberId, visibility, createdAt);
		}
	}

}
