package app.giftify.shared.domain.event.member;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class MemberUpdatedEvent extends BaseDomainEvent {
    private Long memberId;
    private String nickname;
    private String name;
    private String email;
    private String phoneNum;
    private String address;

    public MemberUpdatedEvent(Long memberId, String nickname, String name, String email, String phoneNum, String address) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
        this.address = address;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "MemberSignedEvent{" +
                "memberId=" + memberId +
                ", nickname='" + nickname + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
