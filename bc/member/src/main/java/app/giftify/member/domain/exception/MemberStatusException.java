package app.giftify.member.domain.exception;

public class MemberStatusException extends MemberDomainException {

    public MemberStatusException() {
        super(MemberErrorCode.MEMBER_STATUS_NOT_ACTIVE, "현재 활동 중인 회원이 아닙니다.");
    }
}
