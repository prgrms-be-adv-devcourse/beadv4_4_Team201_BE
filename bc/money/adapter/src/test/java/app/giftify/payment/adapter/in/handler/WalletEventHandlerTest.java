package app.giftify.payment.adapter.in.handler;

import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import app.giftify.shared.domain.event.payment.PaymentType;
import app.giftify.shared.domain.vo.Money;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import domain.errorCode.WalletErrorCode;
import domain.exception.DuplicateTransactionException;
import domain.exception.EventIgnoreException;
import domain.exception.WalletException;
import domain.exception.WalletNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import wallet.service.WalletService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletEventHandlerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletEventHandler handler;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    private PaymentSucceededEvent event = new PaymentSucceededEvent(
            1L,
            "PAYMENT",
            2L,
            Money.of(1000L),
            PaymentType.CHARGE,
            LocalDateTime.now()
    );

    @Test
    @DisplayName("정상 CHARGE 이벤트 처리 시 WalletService.charge 호출")
    void shouldProcessChargeSuccessfully() {
        assertDoesNotThrow(() -> handler.handle(event));
        verify(walletService, times(1)).charge(
                event.getUserId(),
                event.getAmount(),
                event.getType().name(),
                event.getSourceType(),
                event.getPaymentId()
        );
    }

    @Test
    @DisplayName("DuplicateTransactionException 발생 시 EventIgnoreException으로 래핑")
    void shouldWrapDuplicateTransactionException() {
        doThrow(new DuplicateTransactionException("PAYMENT", 1L))
                .when(walletService).charge(
                        event.getUserId(),
                        event.getAmount(),
                        event.getType().name(),
                        event.getSourceType(),
                        event.getPaymentId()
                );

        EventIgnoreException ex = assertThrows(EventIgnoreException.class, () -> handler.handle(event));
        assertTrue(ex.getCause() instanceof DuplicateTransactionException);
    }

    @Test
    @DisplayName("WalletNotFoundException 발생 시 EventIgnoreException으로 래핑")
    void shouldWrapWalletNotFoundException() {
        doThrow(new WalletNotFoundException(3L))
                .when(walletService).charge(
                        event.getUserId(),
                        event.getAmount(),
                        event.getType().name(),
                        event.getSourceType(),
                        event.getPaymentId()
                );

        EventIgnoreException ex = assertThrows(EventIgnoreException.class, () -> handler.handle(event));
        assertTrue(ex.getCause() instanceof WalletNotFoundException);
    }

    @Test
    @DisplayName("WalletException 발생 시 log.warn 호출, 예외 전파 없음")
    void shouldLogWarnOnWalletException() {
        setUpAppender();

        doThrow(new WalletException(WalletErrorCode.INSUFFICIENT_BALANCE))
                .when(walletService)
                .charge(
                        event.getUserId(),
                        event.getAmount(),
                        event.getType().name(),
                        event.getSourceType(),
                        event.getPaymentId()
                );

        // 예외가 전파되지 않는지 확인
        assertDoesNotThrow(() -> handler.handle(event));

        // 로그 캡처
        ArgumentCaptor<ILoggingEvent> captor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(mockAppender).doAppend(captor.capture());  // 이제 로그 발생 후 검증
        ILoggingEvent logEvent = captor.getValue();

        assertThat(logEvent.getLevel()).isEqualTo(Level.WARN);
        assertThat(logEvent.getFormattedMessage()).contains("Business domain exception occurred.");
    }

//    todo: 테스터 수정
//    @Test
//    @DisplayName("지원하지 않는 결제 타입 시 EventIgnoreException 발생")
//    void shouldThrowEventIgnoreOnUnsupportedType() {
//        EventIgnoreException ex = assertThrows(EventIgnoreException.class, () -> handler.handle(event));
//        assertTrue(ex.getCause() instanceof IllegalArgumentException);
//    }

    private void setUpAppender() {
        // Logger를 가져와서 Mock Appender를 부착합니다.
        ch.qos.logback.classic.Logger logger = (Logger) LoggerFactory.getLogger(WalletEventHandler.class);
        logger.addAppender(mockAppender);
    }
}