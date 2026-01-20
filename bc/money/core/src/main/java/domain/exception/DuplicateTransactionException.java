package domain.exception;

/**
 * 이벤트 처리 제어용 예외
 * 무시해도 되는 예외 (정상 시나리오)
 * log.info()
 */
public class DuplicateTransactionException extends RuntimeException {

    public DuplicateTransactionException(String referenceType, Long referenceId) {
        super(String.format("Duplicate transaction. referenceType=%s, referenceId=%d", referenceType, referenceId));
    }
}
