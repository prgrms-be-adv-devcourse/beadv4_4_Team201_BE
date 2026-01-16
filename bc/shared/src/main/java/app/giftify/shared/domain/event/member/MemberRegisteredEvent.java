package app.giftify.shared.domain.event.member;

import app.giftify.shared.domain.event.BaseDomainEvent;

// 회원가입 완료 이벤트
public class MemberRegisteredEvent extends BaseDomainEvent {
    private final Long memberId;
    private final String email;
    private final String authSub;

    public MemberRegisteredEvent(Long memberId, String email, String authSub) {
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
        return "MemberRegisteredEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", occurredAt=" + getOccurredAt() +
                ", memberId=" + memberId +
                ", email='" + email + '\'' +
                ", authSub='" + authSub + '\'' +
                '}';
    }
}
