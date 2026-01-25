package app.giftify.shared.domain.vo;

import app.giftify.shared.domain.type.PaymentMethodType;

import java.time.LocalDateTime;
import java.util.Objects;

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