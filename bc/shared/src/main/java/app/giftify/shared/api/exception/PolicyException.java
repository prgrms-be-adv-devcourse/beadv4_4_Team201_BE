package app.giftify.shared.api.exception;

/**
 * 값은 올바르나 정책 상 실패
 */
public class PolicyException extends BusinessException {
    public PolicyException(ErrorCode errorCode) {
        super(errorCode);
    }
}