package app.giftify.order.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@AllArgsConstructor
@Getter
public enum ResultCode {
    SUCCESS(200, "요청이 성공적으로 처리되었습니다."),
    PARTIAL_SUCCESS(200, "일부 요청이 성공적으로 처리되었습니다."),
    ACCEPTED(202, "요청이 접수되어 처리 중입니다."),
    ALREADY_PROCESSED(200, "이미 처리가 완료된 요청입니다."),
    IN_PROGRESS(202, "현재 동일한 요청이 처리되고 있습니다."),
    FAIL(400, "요청 처리가 불가능합니다."),
    ;

    private final int statusCode;
    private final String message;
}
