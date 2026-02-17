package app.giftify.settlement.domain.status;

public enum SettlementItemStatus {
    CREATED,
    VALIDATING,
    VALIDATED,
    VALIDATE_FAILED,
    READY,
    PROCESSING,
    FAILED,
    MANUAL,
    COMPLETED,
    CANCELLED
}
