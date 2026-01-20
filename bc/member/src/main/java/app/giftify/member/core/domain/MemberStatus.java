package app.giftify.member.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberStatus {
    ACTIVE("활성화", "정상적으로 서비스를 이용 중인 상태"),
    INACTIVE("비활성화", "임시로 서비스 이용이 제한된 상태"),
    DORMANT("휴면", "장기간 미접속으로 인해 휴면 전환된 상태"),
    WITHDRAWN("탈퇴", "회원 탈퇴가 완료된 상태");

    private final String description;
    private final String detail;
}
