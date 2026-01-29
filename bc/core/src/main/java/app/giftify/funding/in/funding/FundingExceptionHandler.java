package app.giftify.funding.in.funding;

import app.giftify.funding.domain.funding.FundingErrorCode;
import app.giftify.funding.domain.funding.FundingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class FundingExceptionHandler {

    /**
     * FundingException 처리
     * - ErrorCode에 정의된 HttpStatus와 메시지를 사용하여 응답
     * - 포맷팅된 메시지(예: ID 포함)도 올바르게 반환
     */
    @ExceptionHandler(FundingException.class)
    public ResponseEntity<?> handleFundingException(FundingException e) {
        FundingErrorCode errorCode = (FundingErrorCode) e.getErrorCode();
        
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(Map.of(
                        "code", errorCode.getCode(),
                        "message", e.getMessage()  // 포맷팅된 메시지 (예: "펀딩을 찾을 수 없습니다. ID: 123")
                ));
    }
}
