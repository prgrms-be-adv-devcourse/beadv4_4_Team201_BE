package app.giftify.payment.adapter.out.jpa.mapper;

import app.giftify.payment.adapter.out.jpa.entity.payment.JpaPayment;
import app.giftify.shared.domain.vo.Money;
import domain.payment.Payment;

public class PaymentMapper {
	public JpaPayment toEntity(Payment domain) {
		return JpaPayment.builder()
			.userId(domain.getUserId())
			.type(domain.getType())
			.status(domain.getStatus())
			.amount(domain.getAmount().amount()) // Money -> BigDecimal
			.pgTransactionId(domain.getPgTransactionId())
			.method(domain.getMethod())
			.build();
	}

	public Payment toDomain(JpaPayment entity) {
		return Payment.builder()
			.paymentId(entity.getId()) // BaseJpaEntity 의 getId() 호출
			.userId(entity.getUserId())
			.type(entity.getType())
			.status(entity.getStatus())
			.amount(Money.of(entity.getAmount()))
			.pgTransactionId(entity.getPgTransactionId())
			.method(entity.getMethod())
			.build();
	}

}
