package domain.payment;

public enum PaymentStatus {
    PENDING,    // 결제 대기
    PAID,       // 결제 완료
    SETTLED,    // 수령 확정 (환불 불가)
    CANCELED,   // 결제 취소
    REFUNDED;   // 환불 완료

    public boolean canRefund() {
        return this == PAID;
    }

    public boolean canCancel() {
        return this == PENDING;
    }
}
