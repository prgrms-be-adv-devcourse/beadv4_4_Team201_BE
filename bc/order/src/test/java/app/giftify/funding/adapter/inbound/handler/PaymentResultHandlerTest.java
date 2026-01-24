package app.giftify.funding.adapter.inbound.handler;

import app.giftify.funding.application.inbound.PaymentResultUseCase;
import app.giftify.shared.domain.event.payment.PaymentCancelledForOrderEvent;
import app.giftify.shared.domain.event.payment.PaymentFailedForOrderEvent;
import app.giftify.shared.domain.event.payment.PaymentRefundedForOrderEvent;
import app.giftify.shared.domain.event.payment.PaymentSucceededForOrderEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentResultHandlerTest {

    @InjectMocks
    private PaymentResultHandler paymentResultHandler;

    @Mock
    private PaymentResultUseCase paymentResultUseCase;

    @Test
    @DisplayName("올바른 결제 성공 이벤트가 전달되면 UseCase를 호출한다")
    void handlePaymentResultSucceed_success() {
        // given
        PaymentSucceededForOrderEvent event = new PaymentSucceededForOrderEvent(1L, "payment-key");

        // when
        paymentResultHandler.handlePaymentResultSucceed(event);

        // then
        verify(paymentResultUseCase, times(1)).completePayment(1L, "payment-key");
    }

    @Test
    @DisplayName("이벤트에 필수 값이 누락된 경우 UseCase를 호출하지 않는다")
    void handlePaymentResultSucceed_fail_missingData() {
        // given
        PaymentSucceededForOrderEvent event = new PaymentSucceededForOrderEvent(null, null);

        // when
        paymentResultHandler.handlePaymentResultSucceed(event);

        // then
        verify(paymentResultUseCase, never()).completePayment(anyLong(), anyString());
    }
    @Test
    @DisplayName("올바른 환불 이벤트가 전달되면 UseCase를 호출한다")
    void handlePaymentResultRefund_success() {
        // given
        PaymentRefundedForOrderEvent event = new PaymentRefundedForOrderEvent(1L, "환불사유");

        // when
        paymentResultHandler.handlePaymentResultRefund(event);

        // then
        verify(paymentResultUseCase, times(1)).refundPayment(1L);
    }

    @Test
    @DisplayName("환불 이벤트에 필수 값이 누락된 경우 UseCase를 호출하지 않는다")
    void handlePaymentResultRefund_fail_missingData() {
        // given
        PaymentRefundedForOrderEvent event = new PaymentRefundedForOrderEvent(null, null);

        // when
        paymentResultHandler.handlePaymentResultRefund(event);

        // then
        verify(paymentResultUseCase, never()).refundPayment(anyLong());
    }

    @Test
    @DisplayName("결제 실패 이벤트가 전달되면 UseCase를 호출한다")
    void handlePaymentResultFail_success() {
        // given
        PaymentFailedForOrderEvent event = new PaymentFailedForOrderEvent(1L);

        // when
        paymentResultHandler.handlePaymentResultFail(event);

        // then
        verify(paymentResultUseCase, times(1)).failPayment(1L);
    }

    @Test
    @DisplayName("결제 취소 이벤트가 전달되면 UseCase를 호출한다")
    void handlePaymentResultCancel_success() {
        // given
        PaymentCancelledForOrderEvent event = new PaymentCancelledForOrderEvent(1L);

        // when
        paymentResultHandler.handlePaymentResultCancel(event);

        // then
        verify(paymentResultUseCase, times(1)).cancelPayment(1L);
    }
}
