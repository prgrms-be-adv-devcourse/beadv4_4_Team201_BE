package app.giftify.member.domain.exception;

import app.giftify.shared.api.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {
    // [000 ~ 099] 공통 및 입력값 유효성
    INVALID_INPUT_VALUE (HttpStatus.BAD_REQUEST.value(), "M001", "유효하지 않은 입력값입니다."),
    INVALID_NICKNAME (HttpStatus.BAD_REQUEST.value(), "M002", "유효하지 않은 닉네임 형식입니다."),

    // [100 ~ 199] 조회 및 리소스 존재 여부
    MEMBER_NOT_FOUND (HttpStatus.NOT_FOUND.value(), "M101", "회원을 찾을 수 없습니다."),

    // [200 ~ 299] 상태 변경 및 비즈니스 흐름 제어
    DUPLICATE_MEMBER (HttpStatus.CONFLICT.value(), "M201", "이미 존재하는 회원입니다."),
    MEMBER_STATUS_NOT_ACTIVE (HttpStatus.CONFLICT.value(), "M202", "현재 활동 중인 회원이 아닙니다."),

    // [300 ~ 399] 친구 및 관계 (추후 확장 대비)
    FRIENDSHIP_ERROR (HttpStatus.BAD_REQUEST.value(), "M301", "친구 관계 처리 중 오류가 발생했습니다."),

    // [900 ~ 999] 시스템 및 내부 오류
    INTERNAL_SERVER_ERROR (HttpStatus.INTERNAL_SERVER_ERROR.value(), "M999", "서버 내부 오류가 발생했습니다.");

    private final int statusCode;
    private final String code;
    private final String message;

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String formatMessage(Object... args) {
        return String.format(this.message, args);
    }
}
