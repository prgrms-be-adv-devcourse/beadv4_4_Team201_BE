package app.giftify.payment.domain.event;

import app.giftify.support.common.event.DomainEvent;
import app.giftify.payment.domain.type.CancelType;
import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.payment.domain.type.PaymentType;
import app.giftify.support.common.money.Money;

import java.time.LocalDateTime;
import java.util.UUID;

// 서비스 레이어가 enrichment하여 integration event로 변환.
public sealed interface PaymentDomainEvent extends DomainEvent {

    record Completed(
            String eventId, LocalDateTime occurredAt,
            Long paymentId, Long orderId, Long memberId, String orderNumber,
            Money paidAmount, PaymentMethod method, PaymentType type,
            String paymentKey, String lastTransactionKey
    ) implements PaymentDomainEvent {
        public Completed(Long paymentId, Long orderId, Long memberId, String orderNumber,
                         Money paidAmount, PaymentMethod method, PaymentType type,
                         String paymentKey, String lastTransactionKey) {
            this(UUID.randomUUID().toString(), LocalDateTime.now(),
                    paymentId, orderId, memberId, orderNumber,
                    paidAmount, method, type, paymentKey, lastTransactionKey);
        }
    }

    record Failed(
            String eventId, LocalDateTime occurredAt,
            Long paymentId, Long orderId, Long memberId, String orderNumber,
            Money paidAmount, PaymentMethod method, PaymentType type
    ) implements PaymentDomainEvent {
        public Failed(Long paymentId, Long orderId, Long memberId, String orderNumber,
                      Money paidAmount, PaymentMethod method, PaymentType type) {
            this(UUID.randomUUID().toString(), LocalDateTime.now(),
                    paymentId, orderId, memberId, orderNumber,
                    paidAmount, method, type);
        }
    }

    record Canceled(
            String eventId, LocalDateTime occurredAt,
            Long paymentId, Long orderId, Long memberId, String orderNumber,
            Money cancelAmount, PaymentMethod method, PaymentType type,
            CancelType cancelType, String reason, String lastTransactionKey
    ) implements PaymentDomainEvent {
        public Canceled(Long paymentId, Long orderId, Long memberId, String orderNumber,
                        Money cancelAmount, PaymentMethod method, PaymentType type,
                        CancelType cancelType, String reason, String lastTransactionKey) {
            this(UUID.randomUUID().toString(), LocalDateTime.now(),
                    paymentId, orderId, memberId, orderNumber,
                    cancelAmount, method, type, cancelType, reason, lastTransactionKey);
        }
    }

    record PartialCanceled(
            String eventId, LocalDateTime occurredAt,
            Long paymentId, Long orderId, Long memberId, String orderNumber,
            Money cancelAmount, PaymentMethod method, PaymentType type,
            String reason, String lastTransactionKey
    ) implements PaymentDomainEvent {
        public PartialCanceled(Long paymentId, Long orderId, Long memberId, String orderNumber,
                               Money cancelAmount, PaymentMethod method, PaymentType type,
                               String reason, String lastTransactionKey) {
            this(UUID.randomUUID().toString(), LocalDateTime.now(),
                    paymentId, orderId, memberId, orderNumber,
                    cancelAmount, method, type, reason, lastTransactionKey);
        }
    }

    record CancelFailed(
            String eventId, LocalDateTime occurredAt,
            Long paymentId, Long orderId, Long memberId, String orderNumber,
            PaymentMethod method, PaymentType type, String errorMessage
    ) implements PaymentDomainEvent {
        public CancelFailed(Long paymentId, Long orderId, Long memberId, String orderNumber,
                            PaymentMethod method, PaymentType type, String errorMessage) {
            this(UUID.randomUUID().toString(), LocalDateTime.now(),
                    paymentId, orderId, memberId, orderNumber,
                    method, type, errorMessage);
        }
    }
}

