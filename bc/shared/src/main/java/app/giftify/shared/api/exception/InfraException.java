package app.giftify.shared.api.exception;

/**
 * 시스템이 일을 못한 것
 */
public class InfraException extends BaseException {
    public InfraException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InfraException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public InfraException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    @Override
    public InfraErrorCode getErrorCode() {
        return (InfraErrorCode) super.getErrorCode();
    }

    public InfraException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause.getMessage(), cause);
    }
}