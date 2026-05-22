package app.giftify.member.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @deprecated Use {@link app.giftify.shared.domain.type.MemberRole} instead.
 */
@Deprecated(forRemoval = true)
@Getter
@RequiredArgsConstructor
public enum MemberRole {
	BUYER("구매자"),
	SELLER("판매자"),
	ADMIN("관리자");

	private final String description;
}
