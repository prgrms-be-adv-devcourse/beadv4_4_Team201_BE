package app.giftify.settlement.domain.status;

public enum SettlementItemStatus {
    CREATED,
    VALIDATING,
    VALIDATE_FAILED,
    READY,
    PROCESSING,
    FAILED,
    MANUAL,
    COMPLETED,
    CANCELLED
    ;

    public static boolean isCancelable(SettlementItemStatus status) {
        return status == CREATED;
    }
}
