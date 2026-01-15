package app.giftify.member.core.exception;

// 멤버 모듈에서 발생하는 모든 비즈니스 예외의 기본 클래스
public abstract class MemberDomainException extends RuntimeException {
    protected MemberDomainException(String message) {
        super(message);
    }
}
