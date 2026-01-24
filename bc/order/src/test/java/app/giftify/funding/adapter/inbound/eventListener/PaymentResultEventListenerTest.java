package app.giftify.funding.adapter.inbound.eventListener;

import app.giftify.funding.adapter.inbound.handler.PaymentResultHandler;
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
}
