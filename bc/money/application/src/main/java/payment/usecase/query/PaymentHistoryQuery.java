package payment.usecase.query;

public record PaymentHistoryQuery(
        Long userId,
        int page,
        int size
) {
}
