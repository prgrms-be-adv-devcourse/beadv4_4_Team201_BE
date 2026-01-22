package app.giftify.payment.adapter.out.jpa.mapper;

import org.springframework.stereotype.Component;

import app.giftify.payment.adapter.out.jpa.entity.payment.JpaPayment;
import app.giftify.shared.domain.vo.Money;
import domain.payment.Payment;

@Component
public class PaymentMapper {
	public JpaPayment toEntity(Payment domain) {
		return JpaPayment.builder()
			.userId(domain.getUserId())
			.orderUuid(domain.getOrderUuid())
			.type(domain.getType())
			.status(domain.getStatus())
			.amount(domain.getAmount().amount()) // Money -> BigDecimal
			.paymentKey(domain.getPaymentKey())
			.method(domain.getMethod())
			.walletUsedAmount(domain.getWalletUsedAmount() != null
				? domain.getWalletUsedAmount().amount() : null)
			.build();
	}

	public Payment toDomain(JpaPayment entity) {
		return Payment.builder()
			.paymentId(entity.getId()) // BaseJpaEntity 의 getId() 호출
			.userId(entity.getUserId())
			.orderUuid(entity.getOrderUuid())
			.type(entity.getType())
			.status(entity.getStatus())
			.amount(Money.of(entity.getAmount()))
			.paymentKey(entity.getPaymentKey())
			.method(entity.getMethod())
			.walletUsedAmount(entity.getWalletUsedAmount() != null
				? Money.of(entity.getWalletUsedAmount()) : null)
			.build();
	}

}
