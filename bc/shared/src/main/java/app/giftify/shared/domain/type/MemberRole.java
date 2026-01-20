package app.giftify.shared.domain.type;

public enum MemberRole {
	BUYER("구매자"),
	SELLER("판매자"),
	ADMIN("관리자");

	private final String description;

	MemberRole(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
