package app.giftify.payment.adapter.outbound.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.payment.adapter.outbound.jpa.entity.JpaCancel;

public interface JpaCancelRepository extends JpaRepository<JpaCancel, Long> {
	List<JpaCancel> findAllByPaymentId(Long paymentId);
}
