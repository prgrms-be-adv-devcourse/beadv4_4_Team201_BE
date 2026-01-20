package app.giftify.member.core.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole {
    BUYER("구매자"),
    SELLER("판매자"),
    ADMIN("관리자");

    private final String description;
}
