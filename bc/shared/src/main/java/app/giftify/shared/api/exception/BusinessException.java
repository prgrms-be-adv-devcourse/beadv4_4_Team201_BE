package app.giftify.shared.api.exception;

/**
 * "의도된 실패"의 최상위
 */
public abstract class BusinessException extends BaseException {
    protected BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    protected BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}