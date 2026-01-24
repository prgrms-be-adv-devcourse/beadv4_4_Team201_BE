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
        Objects.requireNonNull(paymentKey, "paymentKey는 필수입니다.");
        Objects.requireNonNull(transactionKey, "transactionKey는 필수입니다.");
        Objects.requireNonNull(paymentMethodType, "paymentMethodType은 필수입니다.");
        Objects.requireNonNull(paidAt, "paidAt는 필수입니다.");
    }
}