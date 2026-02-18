package app.giftify.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.QueryTimeoutException;

import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

@ExtendWith(MockitoExtension.class)
class BulkPaymentQueryServiceTest {

	private static final List<PaymentStatus> SETTLED_STATUSES = List.of(
		PaymentStatus.PAID, PaymentStatus.RECEIVED, PaymentStatus.REFUNDED
	);

	@Mock
	private PaymentRepository paymentRepository;

	@InjectMocks
	private BulkPaymentQueryService sut;

	@Test
	@DisplayName("정상 결제 건의 순 금액을 반환한다")
	void getBulkAmounts_returnsNetAmounts() {
		Long orderId1 = 101L;
		Long orderId2 = 102L;

		Payment payment1 = buildPayment(1L, orderId1, "ORD-AAA", Money.of(50000), Money.of(10000), PaymentStatus.PAID);
		Payment payment2 = buildPayment(2L, orderId2, "ORD-BBB", Money.of(30000), Money.zero(), PaymentStatus.PAID);

		when(paymentRepository.findAllByOrderIdInAndStatusIn(List.of(orderId1, orderId2), SETTLED_STATUSES))
			.thenReturn(List.of(payment1, payment2));

		Map<Long, Money> result = sut.getBulkAmounts(List.of(orderId1, orderId2));

		assertThat(result).hasSize(2);
		assertThat(result.get(orderId1)).isEqualTo(Money.of(40000));
		assertThat(result.get(orderId2)).isEqualTo(Money.of(30000));
	}

	@Test
	@DisplayName("존재하지 않는 orderId는 Map에서 제외된다")
	void getBulkAmounts_excludesMissingOrders() {
		when(paymentRepository.findAllByOrderIdInAndStatusIn(List.of(999L), SETTLED_STATUSES))
			.thenReturn(List.of());

		Map<Long, Money> result = sut.getBulkAmounts(List.of(999L));

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("전액 환불된 건은 netAmount=0으로 반환한다")
	void getBulkAmounts_fullRefundReturnsZero() {
		Payment payment = buildPayment(1L, 101L, "ORD-CCC", Money.of(50000), Money.of(50000), PaymentStatus.REFUNDED);

		when(paymentRepository.findAllByOrderIdInAndStatusIn(List.of(101L), SETTLED_STATUSES))
			.thenReturn(List.of(payment));

		Map<Long, Money> result = sut.getBulkAmounts(List.of(101L));

		assertThat(result.get(101L)).isEqualTo(Money.zero());
	}

	@Test
	@DisplayName("같은 orderId에 복수 Payment가 있으면 합산한다")
	void getBulkAmounts_aggregatesMultiplePaymentsPerOrder() {
		Long orderId = 101L;
		Payment payment1 = buildPayment(1L, orderId, "ORD-D1", Money.of(30000), Money.zero(), PaymentStatus.PAID);
		Payment payment2 = buildPayment(2L, orderId, "ORD-D2", Money.of(20000), Money.of(5000), PaymentStatus.PAID);

		when(paymentRepository.findAllByOrderIdInAndStatusIn(List.of(orderId), SETTLED_STATUSES))
			.thenReturn(List.of(payment1, payment2));

		Map<Long, Money> result = sut.getBulkAmounts(List.of(orderId));

		assertThat(result.get(orderId)).isEqualTo(Money.of(45000));
	}

	@Test
	@DisplayName("일시적 DB 장애 시 재시도 후 성공한다")
	void getBulkAmounts_retriesOnTransientException() {
		Payment payment = buildPayment(1L, 101L, "ORD-EEE", Money.of(10000), Money.zero(), PaymentStatus.PAID);

		when(paymentRepository.findAllByOrderIdInAndStatusIn(anyList(), anyList()))
			.thenThrow(new QueryTimeoutException("timeout"))
			.thenReturn(List.of(payment));

		Map<Long, Money> result = sut.getBulkAmounts(List.of(101L));

		assertThat(result.get(101L)).isEqualTo(Money.of(10000));
		verify(paymentRepository, times(2)).findAllByOrderIdInAndStatusIn(anyList(), anyList());
	}

	@Test
	@DisplayName("일시적 DB 장애가 최대 재시도 횟수를 초과하면 예외를 던진다")
	void getBulkAmounts_throwsAfterMaxRetries() {
		when(paymentRepository.findAllByOrderIdInAndStatusIn(anyList(), anyList()))
			.thenThrow(new QueryTimeoutException("timeout"));

		assertThatThrownBy(() -> sut.getBulkAmounts(List.of(101L)))
			.isInstanceOf(QueryTimeoutException.class);

		verify(paymentRepository, times(3)).findAllByOrderIdInAndStatusIn(anyList(), anyList());
	}

	private Payment buildPayment(Long id, Long orderId, String orderNumber,
		Money paidAmount, Money refundedAmount, PaymentStatus status) {
		return Payment.builder()
			.id(id).orderId(orderId).orderNumber(orderNumber)
			.memberId(1L).type(PaymentType.DEPOSIT_CHARGE).method(PaymentMethod.DEPOSIT)
			.originAmount(paidAmount).paidAmount(paidAmount)
			.refundedAmount(refundedAmount).status(status)
			.orderItems(List.of()).build();
	}
}
