package app.giftify.payment.adapter.out.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.payment.adapter.out.jpa.entity.JpaPayment;

public interface JpaPaymentRepository extends JpaRepository<JpaPayment, Long> {
}
