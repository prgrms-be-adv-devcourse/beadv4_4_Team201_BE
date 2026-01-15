package app.giftify.member.core.exception;

// 존재하지 않는 회원을 조회하려고 할 때 발생하는 예외
public class MemberNotFoundException extends MemberDomainException {
    public MemberNotFoundException(Long memberId) {
        super("해당 ID의 회원을 찾을 수 없습니다: " + memberId);
    }

    public MemberNotFoundException(String email) {
        super("해당 이메일의 회원을 찾을 수 없습니다: " + email);
    }
}
