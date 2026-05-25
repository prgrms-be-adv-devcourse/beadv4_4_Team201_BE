package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.payment.domain.type.PaymentType;
import app.giftify.support.common.money.Money;

public record CreatePaymentCommand(
        Long memberId,
        Long orderId,
        String orderNumber,
        PaymentType paymentType,
        PaymentMethod method,
        Money expectedAmount,
        Money walletDeductAmount
        ) {
    public CreatePaymentCommand {
        if (memberId == null) {
            throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
                    "[CreatePaymentCommand] memberId는 필수입니다.");
        }
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
                    "[CreatePaymentCommand] orderNumber는 필수입니다.");
        }
        if (paymentType == null) {
            throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
                    "[CreatePaymentCommand] paymentType는 필수입니다.");
        }
        if (method == null) {
            throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
                    "[CreatePaymentCommand] method는 필수입니다.");
        }
        if (expectedAmount == null) {
            throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
                    "[CreatePaymentCommand] expectedAmount는 필수입니다.");
        }
        if (walletDeductAmount == null) {
            throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
                    "[CreatePaymentCommand] walletDeductAmount는 필수입니다.");
        }
        if (walletDeductAmount.isGreaterThan(expectedAmount)) {
            throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
                    "[CreatePaymentCommand] walletDeductAmount는 expectedAmount를 초과할 수 없습니다.");
        }
    }

    public static CreatePaymentCommand of(
            Long memberId, Long orderId, String orderNumber,
            PaymentType paymentType, PaymentMethod method,
            Money expectedAmount
    ) {
        return new CreatePaymentCommand(
                memberId, orderId, orderNumber, paymentType, method, expectedAmount,
                Money.zero());
    }

    public static CreatePaymentCommand withWalletDeduct(
            Long memberId, Long orderId, String orderNumber,
            PaymentType paymentType, PaymentMethod method,
            Money expectedAmount, Money walletDeductAmount

            ) {
        return new CreatePaymentCommand(
                memberId, orderId, orderNumber, paymentType, method, expectedAmount,
                walletDeductAmount);
    }

}
