package app.giftify.member.core.domain.exception;

public class InvalidNicknameException extends MemberDomainException {
    public InvalidNicknameException() {
        super(MemberErrorCode.INVALID_NICKNAME, MemberErrorCode.INVALID_NICKNAME.getMessage());
    }
}
