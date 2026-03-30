package app.giftify.payment.application;

import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.event.PaymentDomainEvent;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.payment.*;
import app.giftify.shared.domain.type.CancelType;
import app.giftify.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentModuleEventPublisher {
    private final EventPublisher eventPublisher;

    // domain event → integration event 변환 + cross-BC enrichment + 발행
    // originalPayment: 상태전이 전 Payment (walletDeductedAmount 등 cross-BC 데이터 소스)
    public void publishFrom(Payment transitionedPayment, Payment originalPayment) {
        var domainEvent = (PaymentDomainEvent) transitionedPayment.pullEvents().getFirst();
        Money walletDeducted = originalPayment.getWalletDeductedAmount();

        Object integrationEvent = switch (domainEvent) {
            case PaymentDomainEvent.Completed e -> PaymentSucceededEvent.create(
                    new PaymentSuccessData(
                            e.paymentId(), e.orderId(), e.memberId(), e.orderNumber(),
                            e.paidAmount(), e.method(), e.type(),
                            e.paymentKey(), e.lastTransactionKey()
                    )
            );
            case PaymentDomainEvent.Failed e -> PaymentFailedEvent.create(
                    new PaymentFailureData(
                            e.paymentId(), e.orderId(), e.memberId(), e.orderNumber(),
                            e.paidAmount(), walletDeducted,
                            e.method(), e.type()
                    )
            );
            case PaymentDomainEvent.Canceled e -> PaymentCanceledEvent.create(
                    new PaymentCancelData(
                            e.paymentId(), e.orderId(), e.memberId(), e.orderNumber(),
                            e.cancelAmount(), walletDeducted,
                            e.method(), e.type(),
                            e.cancelType(), e.reason(), e.lastTransactionKey()
                    )
            );
            case PaymentDomainEvent.PartialCanceled e -> PaymentCanceledEvent.create(
                    new PaymentCancelData(
                            e.paymentId(), e.orderId(), e.memberId(), e.orderNumber(),
                            e.cancelAmount(), walletDeducted,
                            e.method(), e.type(),
                            CancelType.REFUND, e.reason(), e.lastTransactionKey()
                    )
            );
            case PaymentDomainEvent.CancelFailed e -> PaymentCancelFailedEvent.create(
                    new PaymentCancelFailedData(
                            e.paymentId(), e.orderId(), e.memberId(), e.orderNumber(),
                            e.method(), e.type(), e.errorMessage()
                    )
            );
        };

        eventPublisher.publish(integrationEvent);
    }

}
