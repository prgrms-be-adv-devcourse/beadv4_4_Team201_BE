package app.giftify.member.core.domain.exception.member;

// 존재하지 않는 회원을 조회하려고 할 때 발생하는 예외
public class MemberNotFoundException extends MemberDomainException {

    public MemberNotFoundException(Long memberId) {
        super(MemberErrorCode.MEMBER_NOT_FOUND, "해당 ID의 회원을 찾을 수 없습니다: " + memberId);
    }

    public MemberNotFoundException(String emailOrSub) {
        super(MemberErrorCode.MEMBER_NOT_FOUND, "해당 정보의 회원을 찾을 수 없습니다: " + emailOrSub);
    }
}
