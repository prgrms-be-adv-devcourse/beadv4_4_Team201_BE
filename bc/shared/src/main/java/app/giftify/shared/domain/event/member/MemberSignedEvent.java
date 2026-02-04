package app.giftify.shared.domain.event.member;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class MemberSignedEvent extends BaseDomainEvent {
    private final Long memberId;
    private final String authSub;
    private final String nickname;

    public MemberSignedEvent(Long memberId, String authSub, String nickname) {
        super();
        this.memberId = memberId;
        this.authSub = authSub;
        this.nickname = nickname;
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

    @Override
    public String toString() {
        return "MemberSignedEvent{" +
                "memberId=" + memberId +
                ", authSub='" + authSub + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
