package app.giftify.member.domain.exception;

public class InvalidNicknameException extends MemberDomainException {
    public InvalidNicknameException() {
        super(MemberErrorCode.INVALID_NICKNAME, MemberErrorCode.INVALID_NICKNAME.getMessage());
    }
}
