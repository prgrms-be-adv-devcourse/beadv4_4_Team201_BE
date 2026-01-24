package payment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.payment.PaymentCanceledEvent;
import app.giftify.shared.domain.event.payment.PaymentFailedEvent;
import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;
import domain.payment.CancelReason;
import domain.payment.Payment;
import domain.payment.PaymentCreateContext;
import domain.payment.PaymentException;
import domain.payment.PaymentPolicy;
import domain.payment.PaymentRepository;
import domain.payment.PaymentStatus;
import payment.usecase.command.CancelPaymentCommand;
import payment.usecase.command.PaymentChargeCommand;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private EventPublisher eventPublisher;

	@Mock
	private PaymentPolicy paymentPolicy;

	private PaymentService paymentService;

	@BeforeEach
	void setUp() {
		// 기본적으로 정책 하나가 있는 상태로 설정
		paymentService = new PaymentService(paymentRepository, List.of(paymentPolicy), eventPublisher);
	}

	@Test
	@DisplayName("결제 충전: 지원하지 않는 결제 타입이면 예외가 발생한다")
	void charge_ShouldThrowException_WhenPolicyNotSupported() {
		PaymentService emptyPolicyService = new PaymentService(paymentRepository, Collections.emptyList(),
			eventPublisher);
		PaymentChargeCommand command = new PaymentChargeCommand(1L, Money.of(10000));

		assertThatThrownBy(() -> emptyPolicyService.charge(command))
			.isInstanceOf(PaymentException.class)
			.hasMessageContaining("[Payment] 지원하지 않는 결제 타입입니다"); // 포함 여부만 확인
	}

	@Test
	@DisplayName("결제 완료: 결제 내역을 찾을 수 없으면 예외가 발생한다")
	void complete_ShouldThrowException_WhenPaymentNotFound() {
		Long paymentId = 1L;
		given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> paymentService.complete(paymentId, "pg_123", true))
			.isInstanceOf(PaymentException.class)
			.hasMessageContaining("[Payment] 결제 내역을 찾을 수 없습니다");
	}

	@Test
	@DisplayName("결제 완료: 결제 대기 상태가 아니면 완료 처리 시 예외가 발생한다")
	void complete_ShouldThrowException_WhenPaymentNotPending() {
		Long paymentId = 1L;
		Payment payment = Payment.builder()
			.paymentId(paymentId)
			.userId(1L)
			.amount(Money.of(10000))
			.type(PaymentType.POINT_CHARGE)
			.status(PaymentStatus.PAID) // 이미 완료된 상태
			.build();

		given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

		assertThatThrownBy(() -> paymentService.complete(paymentId, "pg_123", true))
			.isInstanceOf(PaymentException.class)
			.hasMessage("[Payment] 결제 대기(PENDING) 상태에서만 완료 처리할 수 있습니다. 현재 상태: PAID");
	}

	@Test
	@DisplayName("결제 충전: 정상적으로 결제가 생성되고 저장된다")
	void charge_ShouldCreatePayment_WhenPolicyValid() {
		// Given
		Long userId = 1L;
		Money amount = Money.of(10000);
		PaymentChargeCommand command = new PaymentChargeCommand(userId, amount);

		// Mocking: 정책 지원 및 검증 통과
		given(paymentPolicy.support(PaymentType.POINT_CHARGE)).willReturn(true);
		willDoNothing().given(paymentPolicy).validate(any(PaymentCreateContext.class));

		// Mocking: 저장 시 ID 부여된 객체 반환
		given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> {
			Payment p = invocation.getArgument(0);
			return p.withId(100L); // 저장 후 ID 100 할당
		});

		// When
		var result = paymentService.charge(command);

		// Then
		assertThat(result.paymentId()).isEqualTo(100L);
		assertThat(result.amount()).isEqualTo(amount);
		assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);

		verify(paymentPolicy).validate(any(PaymentCreateContext.class));
		verify(paymentRepository).save(any(Payment.class));
	}

	@Test
	@DisplayName("결제 완료: PG사 승인 성공 시 상태가 PAID로 변경되고 성공 이벤트가 발행된다")
	void complete_ShouldMarkAsPaid_WhenSuccess() {
		// Given
		Long paymentId = 1L;
		String pgTxId = "pg_key_123";
		Payment payment = Payment.create(1L, PaymentType.POINT_CHARGE, Money.of(10000), null).withId(paymentId);

		given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

		// When
		paymentService.complete(paymentId, pgTxId, true);

		// Then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
		assertThat(payment.getPaymentKey()).isEqualTo(pgTxId);

		verify(paymentRepository).save(payment);
		verify(eventPublisher).publish(any(PaymentSucceededEvent.class));
	}

	@Test
	@DisplayName("결제 완료: PG사 승인 거절 시 상태가 FAILED로 변경되고 실패 이벤트가 발행된다")
	void complete_ShouldMarkAsFailed_WhenPgReject() {
		// Given
		Long paymentId = 1L;
		Payment payment = Payment.create(1L, PaymentType.POINT_CHARGE, Money.of(10000), null).withId(paymentId);

		given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

		// When
		paymentService.complete(paymentId, null, false); // isSuccess = false

		// Then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);

		verify(paymentRepository).save(payment);
		verify(eventPublisher).publish(any(PaymentFailedEvent.class));
	}

	@Test
	@DisplayName("결제 취소: 정상적으로 결제가 취소되고 이벤트가 발행된다")
	void cancel_ShouldCancelPayment_AndPublishEvent() {
		// Given
		Long paymentId = 1L;
		Payment payment = Payment.create(1L, PaymentType.POINT_CHARGE, Money.of(10000), null)
			.withId(paymentId); // PENDING 상태로 생성

		given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

		// When
		paymentService.cancel(new CancelPaymentCommand(paymentId, 1L, CancelReason.USER_REQUEST));

		// Then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
		verify(paymentRepository).save(payment);
		verify(eventPublisher).publish(any(PaymentCanceledEvent.class));
	}

	@Test
	@DisplayName("결제 취소: 이미 취소 불가능한 상태면 아무 동작도 하지 않는다")
	void cancel_ShouldDoNothing_WhenNotCancelable() {
		// Given
		Long paymentId = 1L;
		Payment payment = Payment.builder()
			.paymentId(paymentId)
			.status(PaymentStatus.PAID) // 이미 PAID 상태
			.build();

		given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

		// When
		paymentService.cancel(new CancelPaymentCommand(paymentId, 1L, CancelReason.USER_REQUEST));

		// Then
		// 상태 변경 시도는 없어야 함 (save 호출 안됨 or 상태 유지)
		verify(paymentRepository, never()).save(payment); // 저장 호출이 없어야 함 (변경사항 없으므로)
		verify(eventPublisher, never()).publish(any(PaymentCanceledEvent.class));
	}

	@Test
	@DisplayName("결제 취소: 이미 취소된(CANCELED) 결제는 로그를 남기고 무시된다 (예외 발생 안 함)")
	void cancel_ShouldIgnore_WhenAlreadyCanceled() {
		// Given
		Long paymentId = 1L;
		Payment payment = Payment.builder()
			.paymentId(paymentId)
			.status(PaymentStatus.CANCELED) // 이미 취소된 상태
			.build();

		given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

		// When
		paymentService.cancel(new CancelPaymentCommand(paymentId, 1L, CancelReason.USER_REQUEST));

		// Then
		// 1. Repository 저장 호출이 없어야 함 (상태 변경 안 함)
		verify(paymentRepository, never()).save(any());
		// 2. 이벤트 발행이 없어야 함
		verify(eventPublisher, never()).publish(any());
	}

	@Test
	@DisplayName("결제 취소: 이미 결제 완료된(PAID) 건은 취소 불가하므로 무시된다")
	void cancel_ShouldIgnore_WhenStatusIsPaid() {
		// Given
		Long paymentId = 1L;
		Payment payment = Payment.builder()
			.paymentId(paymentId)
			.status(PaymentStatus.PAID) // 결제 완료 상태
			.build();

		given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

		// When
		paymentService.cancel(new CancelPaymentCommand(paymentId, 1L, CancelReason.USER_REQUEST));

		// Then
		verify(paymentRepository, never()).save(any());
		verify(eventPublisher, never()).publish(any());
	}
}
