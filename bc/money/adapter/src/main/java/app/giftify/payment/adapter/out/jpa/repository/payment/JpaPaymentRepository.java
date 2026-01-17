package app.giftify.payment.adapter.out.jpa.repository.payment;

import app.giftify.payment.adapter.out.jpa.entity.payment.JpaPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPaymentRepository extends JpaRepository<JpaPayment, Long> {
}
