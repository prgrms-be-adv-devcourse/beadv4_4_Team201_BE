package app.giftify.member.core.domain.exception.member;

public class InvalidNicknameException extends MemberDomainException {
    public InvalidNicknameException() {
        super(MemberErrorCode.INVALID_NICKNAME, MemberErrorCode.INVALID_NICKNAME.getMessage());
    }
}
