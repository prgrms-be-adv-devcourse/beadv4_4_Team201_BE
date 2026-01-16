package payment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
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
import app.giftify.shared.domain.event.payment.PaymentType;
import app.giftify.shared.domain.vo.Money;
import domain.payment.Payment;
import domain.payment.PaymentPolicy;
import domain.payment.PaymentRepository;
import domain.payment.PaymentStatus;
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
    @DisplayName("충전: 지원하지 않는 결제 타입이면 예외가 발생한다")
    void charge_ShouldThrowException_WhenPolicyNotSupported() {
        PaymentService emptyPolicyService = new PaymentService(paymentRepository, Collections.emptyList(), eventPublisher);
        PaymentChargeCommand command = new PaymentChargeCommand(1L, Money.of(10000));

        assertThatThrownBy(() -> emptyPolicyService.charge(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("[Payment] 지원하지 않는 결제 타입입니다");
    }

    @Test
    @DisplayName("결제 완료: 결제 내역을 찾을 수 없으면 예외가 발생한다")
    void complete_ShouldThrowException_WhenPaymentNotFound() {
        Long paymentId = 1L;
        given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.complete(paymentId, "pg_123", true))
            .isInstanceOf(IllegalArgumentException.class)
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
            .type(PaymentType.CHARGE)
            .status(PaymentStatus.PAID) // 이미 완료된 상태
            .createdAt(LocalDateTime.now())
            .build();

        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.complete(paymentId, "pg_123", true))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("[Payment] 결제 대기(PENDING) 상태에서만 완료 처리할 수 있습니다.");
    }
}
