package app.giftify.payment.adapter.out.jpa.mapper;

import app.giftify.payment.adapter.out.jpa.entity.JpaPayment;
import app.giftify.shared.domain.vo.Money;
import domain.payment.Payment;

public class PaymentMapper {
	public JpaPayment toEntity(Payment domain) {
		return JpaPayment.builder()
			.userId(domain.getUserId())
			.type(domain.getType())
			.status(domain.getStatus())
			.amount(domain.getAmount().amount()) // Money -> BigDecimal
			.fundingId(domain.getFundingId())
			.pgTransactionId(domain.getPgTransactionId())
			.method(domain.getMethod())
			.paidAt(domain.getPaidAt())
			.refundedAt(domain.getRefundedAt())
			.settledAt(domain.getSettledAt())
			// createdAt은 BaseJpaEntity에서 자동 관리되므로 넘기지 않습니다.
			.build();
	}

	public Payment toDomain(JpaPayment entity) {
		return Payment.builder()
			.paymentId(entity.getId()) // BaseJpaEntity 의 getId() 호출
			.userId(entity.getUserId())
			.type(entity.getType())
			.status(entity.getStatus())
			.amount(Money.ofBigDecimal(entity.getAmount()))
			.fundingId(entity.getFundingId())
			.pgTransactionId(entity.getPgTransactionId())
			.method(entity.getMethod())
			.createdAt(entity.getCreatedAt())
			.paidAt(entity.getPaidAt())
			.refundedAt(entity.getRefundedAt())
			.settledAt(entity.getSettledAt())
			.build();
	}

}
