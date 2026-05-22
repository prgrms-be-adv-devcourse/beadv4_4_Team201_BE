package app.giftify.settlement.application.service;

import app.giftify.settlement.application.inbound.CancelSettlementCommand;
import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.InfraException;
import app.giftify.shared.api.exception.InfraErrorCode;
import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementCancelService 테스트")
class SettlementCancelServiceTest {

    @Mock
    private SettlementItemService settlementItemService;

    @InjectMocks
    private SettlementCancelService settlementCancelService;

    private final CancelSettlementCommand command = new CancelSettlementCommand(10L, 20L);

    @Nested
    @DisplayName("cancel()")
    class CancelTests {

        @Test
        @DisplayName("정상 케이스: settlementItemService.cancel()을 호출한다")
        void given_validCommand_when_cancel_then_delegateToService() {
            // when
            settlementCancelService.cancel(command);

            // then
            verify(settlementItemService).cancel(command);
        }

        @Test
        @DisplayName("BusinessException 발생 시 예외를 삼키고 정상 종료한다")
        void given_businessException_when_cancel_then_swallowException() {
            // given
            doThrow(new DomainException(SettlementErrorCode.INVALID_STATUS_TRANSITION))
                    .when(settlementItemService).cancel(command);

            // when & then
            assertThatCode(() -> settlementCancelService.cancel(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("재시도 가능한 InfraException 발생 시 예외를 상위로 전파한다")
        void given_retryableInfraException_when_cancel_then_propagateException() {
            // given
            doThrow(new InfraException(InfraErrorCode.DB_LOCK_TIMEOUT))
                    .when(settlementItemService).cancel(command);

            // when & then
            assertThatThrownBy(() -> settlementCancelService.cancel(command))
                    .isInstanceOf(InfraException.class);
        }

        @Test
        @DisplayName("재시도 불가 InfraException 발생 시 예외를 삼키고 정상 종료한다")
        void given_nonRetryableInfraException_when_cancel_then_swallowException() {
            // given
            doThrow(new InfraException(InfraErrorCode.DB_CONSTRAINT_VIOLATION))
                    .when(settlementItemService).cancel(command);

            // when & then
            assertThatCode(() -> settlementCancelService.cancel(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("예상치 못한 예외 발생 시 예외를 삼키고 정상 종료한다")
        void given_unexpectedException_when_cancel_then_swallowException() {
            // given
            doThrow(new RuntimeException("unexpected"))
                    .when(settlementItemService).cancel(command);

            // when & then
            assertThatCode(() -> settlementCancelService.cancel(command))
                    .doesNotThrowAnyException();
        }
    }
}