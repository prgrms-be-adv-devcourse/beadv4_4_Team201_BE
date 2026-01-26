package app.giftify.funding.adapter.inbound.eventListener;

import app.giftify.funding.adapter.inbound.handler.PaymentResultHandler;
import app.giftify.shared.domain.event.payment.PaymentCancelledForOrderEvent;
import app.giftify.shared.domain.event.payment.PaymentFailedForOrderEvent;
import app.giftify.shared.domain.event.payment.PaymentRefundedForOrderEvent;
import app.giftify.shared.domain.event.payment.PaymentSucceededForOrderEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = PaymentResultEventListener.class)
@ActiveProfiles("local")
class PaymentResultEventListenerTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private PaymentResultHandler paymentResultHandler;

    @Test
    @DisplayName("결제 성공 이벤트가 발행되면 리스너가 수신하여 핸들러에게 전달한다")
    void handlePaymentResultSucceededEvent_success() {
        // given
        PaymentSucceededForOrderEvent event = new PaymentSucceededForOrderEvent(1L, "payment-key");

        // when
        eventPublisher.publishEvent(event);

        // then
        verify(paymentResultHandler, timeout(1000)).handlePaymentResultSucceed(any(PaymentSucceededForOrderEvent.class));
    }

    @Test
    @DisplayName("환불 이벤트가 발행되면 리스너가 수신하여 핸들러에게 전달한다")
    void handlePaymentResultRefundedEvent_success() {
        // given
        PaymentRefundedForOrderEvent event = new PaymentRefundedForOrderEvent(1L, "환불사유");

        // when
        eventPublisher.publishEvent(event);

        // then
        verify(paymentResultHandler, timeout(1000)).handlePaymentResultRefund(any(PaymentRefundedForOrderEvent.class));
    }

    @Test
    @DisplayName("결제 실패 이벤트가 발행되면 리스너가 수신하여 핸들러에게 전달한다")
    void handlePaymentResultFailedEvent_success() {
        // given
        PaymentFailedForOrderEvent event = new PaymentFailedForOrderEvent(1L);

        // when
        eventPublisher.publishEvent(event);

        // then
        verify(paymentResultHandler, timeout(1000)).handlePaymentResultFail(any(PaymentFailedForOrderEvent.class));
    }

    @Test
    @DisplayName("결제 취소 이벤트가 발행되면 리스너가 수신하여 핸들러에게 전달한다")
    void handlePaymentResultCancelledEvent_success() {
        // given
        PaymentCancelledForOrderEvent event = new PaymentCancelledForOrderEvent(1L);

        // when
        eventPublisher.publishEvent(event);

        // then
        verify(paymentResultHandler, timeout(1000)).handlePaymentResultCancel(any(PaymentCancelledForOrderEvent.class));
    }
}
