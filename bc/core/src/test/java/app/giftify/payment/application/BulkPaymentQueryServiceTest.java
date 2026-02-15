package app.giftify.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

@ExtendWith(MockitoExtension.class)
class BulkPaymentQueryServiceTest {

	@Mock
	private PaymentRepository paymentRepository;

	@InjectMocks
	private BulkPaymentQueryService sut;

	@Test
	@DisplayName("정상 결제 건의 순 금액을 반환한다")
	void getBulkAmounts_returnsNetAmounts() {
		Long orderId1 = 101L;
		Long orderId2 = 102L;

		Payment payment1 = Payment.builder()
			.id(1L).orderId(orderId1).orderNumber("ORD-AAA")
			.memberId(1L).type(PaymentType.DEPOSIT_CHARGE).method(PaymentMethod.DEPOSIT)
			.originAmount(Money.of(50000)).paidAmount(Money.of(50000))
			.refundedAmount(Money.of(10000)).status(PaymentStatus.PAID)
			.orderItems(List.of()).build();
		Payment payment2 = Payment.builder()
			.id(2L).orderId(orderId2).orderNumber("ORD-BBB")
			.memberId(2L).type(PaymentType.DEPOSIT_CHARGE).method(PaymentMethod.DEPOSIT)
			.originAmount(Money.of(30000)).paidAmount(Money.of(30000))
			.refundedAmount(Money.zero()).status(PaymentStatus.PAID)
			.orderItems(List.of()).build();

		when(paymentRepository.findAllByOrderIdIn(List.of(orderId1, orderId2)))
			.thenReturn(List.of(payment1, payment2));

		Map<Long, Money> result = sut.getBulkAmounts(List.of(orderId1, orderId2));

		assertThat(result).hasSize(2);
		assertThat(result.get(orderId1)).isEqualTo(Money.of(40000));
		assertThat(result.get(orderId2)).isEqualTo(Money.of(30000));
	}

	@Test
	@DisplayName("존재하지 않는 orderId는 Map에서 제외된다")
	void getBulkAmounts_excludesMissingOrders() {
		when(paymentRepository.findAllByOrderIdIn(List.of(999L)))
			.thenReturn(List.of());

		Map<Long, Money> result = sut.getBulkAmounts(List.of(999L));

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("전액 환불된 건은 netAmount=0으로 반환한다")
	void getBulkAmounts_fullRefundReturnsZero() {
		Payment payment = Payment.builder()
			.id(1L).orderId(101L).orderNumber("ORD-CCC")
			.memberId(1L).type(PaymentType.DEPOSIT_CHARGE).method(PaymentMethod.DEPOSIT)
			.originAmount(Money.of(50000)).paidAmount(Money.of(50000))
			.refundedAmount(Money.of(50000)).status(PaymentStatus.PAID)
			.orderItems(List.of()).build();

		when(paymentRepository.findAllByOrderIdIn(List.of(101L)))
			.thenReturn(List.of(payment));

		Map<Long, Money> result = sut.getBulkAmounts(List.of(101L));

		assertThat(result.get(101L)).isEqualTo(Money.zero());
	}
}
