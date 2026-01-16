package app.giftify.member.application.port.out;

// 멤버 내의 이벤트 발행하는 포트
public interface MemberEventSpringPublisher {
    void publishMemberRegistered(Long memberId, String email, String authSub);

    void publishMemberLoggedIn(Long memberId, String email, String authSub);
}
