package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

import java.util.List;

public record CreatePaymentCommand(
        Long memberId,
        Long orderId,
        String orderNumber,
        PaymentType paymentType,
        PaymentMethod method,
        Money expectedAmount,
        Money walletDeductAmount,
        List<OrderItemSnapshot> orderItems
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
        if (orderItems == null || orderItems.isEmpty()) {
            throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
                    "[CreatePaymentCommand] orderItems는 필수입니다.");
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


        // 실제 금액 계산 및 검증
        Money itemsTotal = orderItems.stream()
                .map(OrderItemSnapshot::amount)
                .reduce(Money.zero(), Money::plus);

        if (!itemsTotal.equals(expectedAmount)) {
            throw new PaymentException(PaymentErrorCode.AMOUNT_MISMATCH,
                    String.format("[CreatePaymentCommand] 주문 금액이 변경되었습니다. " +
                            "기대 금액: %s, 현재 금액: %s", expectedAmount, itemsTotal));
        }
    }

    public static CreatePaymentCommand of(
            Long memberId, Long orderId, String orderNumber,
            PaymentType paymentType, PaymentMethod method,
            Money expectedAmount, List<OrderItemSnapshot> orderItems
    ) {
        return new CreatePaymentCommand(
                memberId, orderId, orderNumber, paymentType, method, expectedAmount,
                Money.zero(), orderItems);
    }

    public static CreatePaymentCommand withWalletDeduct(
            Long memberId, Long orderId, String orderNumber,
            PaymentType paymentType, PaymentMethod method,
            Money expectedAmount, Money walletDeductAmount,
            List<OrderItemSnapshot> orderItems
    ) {
        return new CreatePaymentCommand(
                memberId, orderId, orderNumber, paymentType, method, expectedAmount,
                walletDeductAmount, orderItems);
    }

}
