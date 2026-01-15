package app.giftify.member.core.exception;

// 친구 관계 처리 중 비즈니스 규칙을 위반했을 때 발생하는 예외
public class FriendshipException extends MemberDomainException {
    public FriendshipException(String message) {
        super(message);
    }
}
