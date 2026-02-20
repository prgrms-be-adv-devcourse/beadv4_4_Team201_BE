package app.giftify.settlement.domain.model;

import app.giftify.settlement.domain.status.SettlementQueueStatus;
import app.giftify.shared.api.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SettlementQueueTest {

    @Mock
    private SettlementItem item;

    @Test
    @DisplayName("생성 시 READY 상태이다")
    void constructor_setsReadyStatus() {
        // given / when
        SettlementQueue queue = new SettlementQueue(item);

        // then
        assertThat(queue.getStatus()).isEqualTo(SettlementQueueStatus.READY);
        assertThat(queue.getItem()).isEqualTo(item);
    }

    @Test
    @DisplayName("READY → IN_PROGRESS 상태 전이에 성공한다")
    void startProcessing_fromReady_succeeds() {
        // given
        SettlementQueue queue = new SettlementQueue(item);

        // when
        queue.startProcessing();

        // then
        assertThat(queue.getStatus()).isEqualTo(SettlementQueueStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("READY가 아닌 상태에서 startProcessing 호출 시 예외가 발생한다")
    void startProcessing_fromNonReady_throwsException() {
        // given
        SettlementQueue queue = new SettlementQueue(item);
        queue.startProcessing();

        // when / then
        assertThatThrownBy(queue::startProcessing)
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("IN_PROGRESS → DONE 상태 전이에 성공하고 item.complete(historyId)가 호출된다")
    void done_fromInProgress_succeeds() {
        // given
        SettlementQueue queue = new SettlementQueue(item);
        queue.startProcessing();
        Long historyId = 100L;

        // when
        queue.done(historyId);

        // then
        assertThat(queue.getStatus()).isEqualTo(SettlementQueueStatus.DONE);
        verify(item).complete(historyId);
    }

    @Test
    @DisplayName("IN_PROGRESS가 아닌 상태에서 done 호출 시 예외가 발생한다")
    void done_fromNonInProgress_throwsException() {
        // given
        SettlementQueue queue = new SettlementQueue(item);

        // when / then
        assertThatThrownBy(() -> queue.done(1L))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("fail 호출 시 FAIL 상태가 된다")
    void fail_setsFailStatus() {
        // given
        SettlementQueue queue = new SettlementQueue(item);

        // when
        queue.fail();

        // then
        assertThat(queue.getStatus()).isEqualTo(SettlementQueueStatus.FAIL);
    }
}
