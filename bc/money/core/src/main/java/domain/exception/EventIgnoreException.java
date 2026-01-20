package domain.exception;

/**
 * 이벤트 처리 제어용 예외
 * 재시도 가치가 없는 비즈니스 예외
 * log.error()
 */
public class EventIgnoreException extends RuntimeException {

    public EventIgnoreException(Throwable cause) {
        super(cause);
    }
}
