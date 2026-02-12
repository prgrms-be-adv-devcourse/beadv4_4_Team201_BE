package app.giftify.payment.adapter.outbound.jpa.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaPayment extends BaseJpaEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 50)
	private PaymentType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "method", nullable = false, length = 50)
	private PaymentMethod method;

	@Column(name = "order_id", nullable = false, length = 255)
	private String orderId;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "origin_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal originAmount;

	@Column(name = "paid_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal paidAmount;

	@Column(name = "refunded_amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal refundedAmount;

	@Column(name = "order_items_json", nullable = false, columnDefinition = "TEXT")
	private String orderItemsJson;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 50)
	private PaymentStatus status;

	@Column(name = "payment_key", length = 255)
	private String paymentKey;

	@Column(name = "last_transaction_key", length = 255)
	private String lastTransactionKey;

	@Column(name = "approve_code", length = 255)
	private String approveCode;

	@Column(name = "paid_at")
	private LocalDateTime paidAt;

	private JpaPayment(
		PaymentType type,
		PaymentMethod method,
		String orderId,
		Long memberId,
		BigDecimal originAmount,
		BigDecimal paidAmount,
		BigDecimal refundedAmount,
		String orderItemsJson,
		PaymentStatus status,
		String paymentKey,
		String lastTransactionKey,
		String approveCode,
		LocalDateTime paidAt
	) {
		this.type = type;
		this.method = method;
		this.orderId = orderId;
		this.memberId = memberId;
		this.originAmount = originAmount;
		this.paidAmount = paidAmount;
		this.refundedAmount = refundedAmount;
		this.orderItemsJson = orderItemsJson;
		this.status = status;
		this.paymentKey = paymentKey;
		this.lastTransactionKey = lastTransactionKey;
		this.approveCode = approveCode;
		this.paidAt = paidAt;
	}

	public static JpaPayment from(Payment payment, ObjectMapper objectMapper) {
		String orderItemsJson;
		try {
			orderItemsJson = objectMapper.writeValueAsString(payment.getOrderItems());
		} catch (JsonProcessingException e) {
			throw new PaymentException(PaymentErrorCode.INTERNAL_SERVER_ERROR,
				"[JpaPayment] orderItems JSON 직렬화 실패", e);
		}

		return new JpaPayment(
			payment.getType(),
			payment.getMethod(),
			payment.getOrderId(),
			payment.getMemberId(),
			payment.getOriginAmount().amount(),
			payment.getPaidAmount().amount(),
			payment.getRefundedAmount().amount(),
			orderItemsJson,
			payment.getStatus(),
			payment.getPaymentKey(),
			payment.getLastTransactionKey(),
			payment.getApproveCode(),
			payment.getPaidAt()
		);
	}

	public Payment toDomain(ObjectMapper objectMapper) {
		OrderItemSnapshot[] orderItems;
		try {
			orderItems = objectMapper.readValue(orderItemsJson, OrderItemSnapshot[].class);
		} catch (JsonProcessingException e) {
			throw new PaymentException(PaymentErrorCode.INTERNAL_SERVER_ERROR,
				"[JpaPayment] orderItems JSON 역직렬화 실패", e);
		}

		return Payment.builder()
			.id(super.getId())
			.type(type)
			.method(method)
			.orderId(orderId)
			.memberId(memberId)
			.originAmount(Money.of(originAmount))
			.paidAmount(Money.of(paidAmount))
			.refundedAmount(Money.of(refundedAmount))
			.orderItems(java.util.Arrays.asList(orderItems))
			.status(status)
			.paymentKey(paymentKey)
			.lastTransactionKey(lastTransactionKey)
			.approveCode(approveCode)
			.paidAt(paidAt)
			.createdAt(super.getCreatedAt())
			.build();
	}

	public void updateFrom(Payment payment, String orderItemsJson) {
		this.status = payment.getStatus();
		this.paymentKey = payment.getPaymentKey();
		this.lastTransactionKey = payment.getLastTransactionKey();
		this.approveCode = payment.getApproveCode();
		this.paidAt = payment.getPaidAt();
		this.refundedAmount = payment.getRefundedAmount().amount();
		this.orderItemsJson = orderItemsJson;
	}
}
