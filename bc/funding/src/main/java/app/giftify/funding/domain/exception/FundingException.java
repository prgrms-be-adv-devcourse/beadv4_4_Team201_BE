package app.giftify.funding.domain.exception;

import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.ErrorCode;

public class FundingException extends DomainException {

    public FundingException(FundingErrorCode errorCode) {
        super(errorCode);
    }

    public FundingException(FundingErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * ErrorCode의 메시지를 포맷팅하여 예외 생성
     * @param errorCode 에러 코드
     * @param args 포맷 인자 (String.format 사용)
     */
    public FundingException(ErrorCode errorCode, Object... args) {
        super(errorCode, String.format(errorCode.getMessage(), args));
    }
}
