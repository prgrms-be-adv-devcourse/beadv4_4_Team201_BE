package app.giftify.member.core.domain.exception;

import app.giftify.shared.api.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {
    MEMBER_NOT_FOUND("M001", "회원을 찾을 수 없습니다."),
    DUPLICATE_MEMBER("M002", "이미 존재하는 회원입니다."),
    FRIENDSHIP_ERROR("M003", "친구 관계 처리 중 오류가 발생했습니다.");

    private final String code;
    private final String message;
}
