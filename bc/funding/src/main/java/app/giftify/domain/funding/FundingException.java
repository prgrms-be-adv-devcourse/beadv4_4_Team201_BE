package app.giftify.domain.funding;

import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.ErrorCode;

public class FundingException extends DomainException {

    public FundingException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FundingException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}

