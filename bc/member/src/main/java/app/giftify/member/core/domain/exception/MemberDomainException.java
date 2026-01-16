package app.giftify.member.core.domain.exception;

import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.ErrorCode;

// 멤버 모듈에서 발생하는 모든 비즈니스 예외의 기본 클래스
public abstract class MemberDomainException extends DomainException {

    protected MemberDomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected MemberDomainException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
