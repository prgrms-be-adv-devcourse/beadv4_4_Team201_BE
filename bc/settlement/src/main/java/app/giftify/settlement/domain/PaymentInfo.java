package app.giftify.settlement.domain;

import java.time.LocalDateTime;

public record PaymentInfo(
    String paymentKey,
    String transactionKey,
    PaymentMethodType paymentMethodType,
    LocalDateTime paidAt
) {
    public PaymentInfo {
        if (paymentKey == null || paymentKey.isBlank()) throw new IllegalArgumentException("paymentKey는 필수입니다.");
        if (transactionKey == null || transactionKey.isBlank()) throw new IllegalArgumentException("transactionKey는 필수입니다.");
        if (paymentMethodType == null) throw new IllegalArgumentException("paymentMethodType은 필수입니다.");
        if (paidAt == null) throw new IllegalArgumentException("paidAt은 필수입니다.");
    }
}