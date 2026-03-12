package app.giftify.shared.domain.event.member;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.type.MemberRole;

public class MemberUpdatedEvent extends BaseDomainEvent {
    private final Long memberId;
    private final String authSub;
    private final String nickname;
    private final MemberRole role;

    public MemberUpdatedEvent(Long memberId, String authSub, String nickname, MemberRole role) {
        super();
        this.memberId = memberId;
        this.authSub = authSub;
        this.nickname = nickname;
        this.role = role;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getAuthSub() {
        return authSub;
    }

    public String getNickname() {
        return nickname;
    }

    public MemberRole getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "MemberSignedEvent{" +
                "memberId=" + memberId +
                ", authSub='" + authSub + '\'' +
                ", nickname='" + nickname + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
