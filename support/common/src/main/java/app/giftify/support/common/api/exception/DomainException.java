package app.giftify.support.common.api.exception;

/**
 * 도메인 검증 실패
 */
public class DomainException extends BusinessException {
    public DomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DomainException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public DomainException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public DomainException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }


}
