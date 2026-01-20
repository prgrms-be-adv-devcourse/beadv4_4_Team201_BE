package app.giftify.payment.adapter.in.handler;

import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import domain.exception.DuplicateTransactionException;
import domain.exception.EventIgnoreException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WalletEventExceptionHandlerTest {

    private WalletEventExceptionHandler exceptionHandler;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Mock
    private PaymentSucceededEvent mockEvent;

    @BeforeEach
    void setUp() {
        exceptionHandler = new WalletEventExceptionHandler();
        
        // Logger를 가져와서 Mock Appender를 부착합니다.
        Logger logger = (Logger) LoggerFactory.getLogger(WalletEventExceptionHandler.class);
        logger.addAppender(mockAppender);
    }

    @AfterEach
    void tearDown() {
        // 테스트가 끝나면 Appender를 제거하여 다른 테스트에 영향을 주지 않도록 합니다.
        Logger logger = (Logger) LoggerFactory.getLogger(WalletEventExceptionHandler.class);
        logger.detachAppender(mockAppender);
    }

    @Test
    @DisplayName("EventIgnoreException + DuplicateTransactionException이면 log.info 호출")
    void shouldLogInfoOnDuplicateTransaction() {
        // given
        Exception e = new EventIgnoreException(new DuplicateTransactionException("PAYMENT", 1L));
        ArgumentCaptor<ILoggingEvent> eventCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);

        // when
        exceptionHandler.handle(mockEvent, e);

        // then
        // 실제로 appender.doAppend()가 호출되었는지 확인
        verify(mockAppender).doAppend(eventCaptor.capture());
        
        ILoggingEvent logEvent = eventCaptor.getValue();
        assertThat(logEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(logEvent.getFormattedMessage()).contains("Duplicate transaction"); // 기대하는 로그 메시지
    }

    @Test
    @DisplayName("EventIgnoreException이 아닌 일반 예외이면 log.error 호출")
    void shouldLogErrorOnGeneralException() {
        // given
        Exception ex = new RuntimeException("fail");
        ArgumentCaptor<ILoggingEvent> eventCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);

        // when
        exceptionHandler.handle(mockEvent, ex);

        // then
        verify(mockAppender).doAppend(eventCaptor.capture());
        
        ILoggingEvent logEvent = eventCaptor.getValue();
        assertThat(logEvent.getLevel()).isEqualTo(Level.ERROR);
    }
}