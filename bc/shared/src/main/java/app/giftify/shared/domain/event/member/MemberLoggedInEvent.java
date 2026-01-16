package app.giftify.shared.domain.event.member;

import app.giftify.shared.domain.event.BaseDomainEvent;

// 로그인 성공 이벤트
public class MemberLoggedInEvent extends BaseDomainEvent {
    private final Long memberId;
    private final String email;
    private final String authSub;

    public MemberLoggedInEvent(Long memberId, String email, String authSub) {
        super();
        this.memberId = memberId;
        this.email = email;
        this.authSub = authSub;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getEmail() {
        return email;
    }

    public String getAuthSub() {
        return authSub;
    }

    @Override
    public String toString() {
        return "MemberLoggedInEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", occurredAt=" + getOccurredAt() +
                ", memberId=" + memberId +
                ", email='" + email + '\'' +
                ", authSub='" + authSub + '\'' +
                '}';
    }
}
