package giftify.support.web.idempotency;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum IdempotencyStatus {
    PROCESSING("요청을 처리 중입니다."),
    COMPLETED("이미 처리가 완료된 요청입니다.")
    ;

    private final String description;
}
