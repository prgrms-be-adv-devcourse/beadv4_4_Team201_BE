package app.giftify.friendship.domain.exception;

import app.giftify.shared.api.exception.DomainException;

public class FriendshipException extends DomainException {

    public FriendshipException(FriendshipErrorCode errorCode) {
        super(errorCode);
    }

    public FriendshipException(FriendshipErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
