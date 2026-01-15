package app.giftify.member.core.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자의 시스템 내 역할을 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum MemberRole {
    BUYER("구매자"),
    SELLER("판매자"),
    ADMIN("관리자");

    private final String description;
}
