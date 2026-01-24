package app.giftify.funding.adapter.inbound.handler;

import app.giftify.funding.application.inbound.PaymentResultUseCase;
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
}
