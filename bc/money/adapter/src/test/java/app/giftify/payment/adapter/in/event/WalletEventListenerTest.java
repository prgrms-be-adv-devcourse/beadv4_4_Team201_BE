package app.giftify.payment.adapter.in.event;

import app.giftify.payment.adapter.in.handler.WalletEventExceptionHandler;
import app.giftify.payment.adapter.in.handler.WalletEventHandler;
import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletEventListenerTest {

    @Mock
    private WalletEventHandler eventHandler;

    @Mock
    private WalletEventExceptionHandler exceptionHandler;

    @InjectMocks
    private WalletEventListener listener;

    @Mock
    private PaymentSucceededEvent mockEvent;

    @Test
    @DisplayName("PaymentSucceededEvent 정상 처리 시, 예외 핸들러는 호출되지 않아야 한다")
    void shouldHandleEventSuccessfully() {

        // when
        listener.on(mockEvent);

        // then
        verify(eventHandler, times(1)).handle(mockEvent);
        verifyNoInteractions(exceptionHandler); // 예외 핸들러는 호출되지 않아야 함
    }

    @Test
    @DisplayName("PaymentSucceededEvent 처리 중 예외 발생 시, 예외 핸들러가 호출되어야 한다")
    void shouldHandleExceptionWhenEventHandlerThrows() {
        RuntimeException ex = mock(RuntimeException.class);
        doThrow(ex).when(eventHandler).handle(mockEvent);

        // when
        listener.on(mockEvent);

        // then
        verify(eventHandler, times(1)).handle(mockEvent);
        verify(exceptionHandler, times(1)).handle(mockEvent, ex);
    }
}