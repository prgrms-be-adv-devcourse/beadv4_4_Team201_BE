package app.giftify.friendship.domain.exception;

import app.giftify.shared.api.exception.ErrorCode;

public enum FriendshipErrorCode implements ErrorCode {
    SELF_FRIEND_REQUEST     (400, "F001", "자기 자신에게 친구 요청을 보낼 수 없습니다."),
    DUPLICATE_FRIENDSHIP    (409, "F002", "이미 친구 관계가 존재합니다."),
    FRIENDSHIP_NOT_FOUND    (404, "F003", "친구 관계를 찾을 수 없습니다."),
    NOT_FRIENDSHIP_RECEIVER (403, "F004", "이 요청을 수락/거절할 권한이 없습니다."),
    NOT_FRIENDSHIP_MEMBER   (403, "F005", "이 친구 관계의 당사자가 아닙니다."),
    MEMBER_NOT_FOUND        (404, "F006", "요청 대상 회원을 찾을 수 없습니다."),
    INVALID_FRIENDSHIP_STATUS(400, "F007", "현재 상태에서 수행할 수 없는 작업입니다."),
    WITHDRAWN_MEMBER(400, "F008", "탈퇴한 회원에게는 친구 요청을 보낼 수 없습니다.");

    private final int statusCode;
    private final String code;
    private final String message;

    FriendshipErrorCode(int statusCode, String code, String message) {
        this.statusCode = statusCode;
        this.code = code;
        this.message = message;
    }

    @Override public int getStatusCode() { return statusCode; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }

    @Override
    public String formatMessage(Object... args) {
        return String.format(this.message, args);
    }
}
