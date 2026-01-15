package app.giftify.member.core.exception;

// 이미 존재하는 회원을 중복으로 가입시키려 할 때 발생하는 예외
public class DuplicateMemberException extends MemberDomainException {

    public DuplicateMemberException(String email) {
        super(MemberErrorCode.DUPLICATE_MEMBER, "이미 가입된 이메일입니다: " + email);
    }
}
