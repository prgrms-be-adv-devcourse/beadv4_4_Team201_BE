package app.giftify.payment.adapter.outbound.jpa;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.giftify.payment.adapter.outbound.jpa.entity.JpaPayment;
import app.giftify.payment.adapter.outbound.jpa.entity.JpaPaymentHistory;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentHistory;

@Component
public class PaymentMapper {

	private final ObjectMapper objectMapper;

	public PaymentMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public JpaPayment toEntity(Payment payment) {
		return JpaPayment.from(payment, objectMapper);
	}

	public Payment toDomain(JpaPayment jpaPayment) {
		return jpaPayment.toDomain(objectMapper);
	}

	public JpaPaymentHistory toEntity(PaymentHistory history, Long paymentId) {
		return JpaPaymentHistory.from(history, paymentId);
	}

	public PaymentHistory toDomain(JpaPaymentHistory jpaHistory) {
		return jpaHistory.toDomain();
	}
}
