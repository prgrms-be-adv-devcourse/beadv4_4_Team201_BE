package app.giftify.payment.adapter.outbound.jpa;

import app.giftify.payment.adapter.outbound.jpa.entity.JpaPayment;
import app.giftify.payment.adapter.outbound.jpa.entity.JpaPaymentHistory;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentHistory;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public JpaPayment toEntity(Payment payment) {
        return JpaPayment.from(payment);
    }

    public Payment toDomain(JpaPayment jpaPayment) {
        return jpaPayment.toDomain();
    }

    public JpaPaymentHistory toEntity(PaymentHistory history, Long paymentId) {
        return JpaPaymentHistory.from(history, paymentId);
    }

    public PaymentHistory toDomain(JpaPaymentHistory jpaHistory) {
        return jpaHistory.toDomain();
    }
}
