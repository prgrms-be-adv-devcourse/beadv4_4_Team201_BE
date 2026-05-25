package app.giftify.settlement.application.service;

import app.giftify.settlement.adapter.outbound.batch.execution.ExecutionResult;
import app.giftify.settlement.application.outbound.port.SettlementHistoryRepository;
import app.giftify.settlement.application.outbound.port.SettlementQueueRepository;
import app.giftify.settlement.domain.model.*;
import app.giftify.support.common.event.EventPublisher;
import app.giftify.support.common.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementExecutionServiceTest {

    @Mock
    private SettlementHistoryRepository settlementHistoryRepository;

    @Mock
    private SettlementQueueRepository settlementQueueRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private SettlementExecutionService settlementExecutionService;

    @Test
    @DisplayName("큐가 존재하면 ExecutionResult를 반환한다")
    void process_withReadyQueues_returnsExecutionResult() {
        // given
        Long sellerId = 1L;
        SettlementItem item = mock(SettlementItem.class);
        SettlementQueue queue = mock(SettlementQueue.class);

        Money paidAmount = Money.of(10000);
        Money platformFee = Money.of(500);
        Money pgFee = Money.of(300);
        Money settlementAmount = Money.of(9200);

        SettlementCore core = new SettlementCore(paidAmount, platformFee, pgFee, settlementAmount);

        when(queue.getItem()).thenReturn(item);
        when(item.getCore()).thenReturn(core);
        when(settlementQueueRepository.findAllReadyQueuesBySellerId(sellerId))
                .thenReturn(List.of(queue));

        // when
        ExecutionResult result = settlementExecutionService.process(sellerId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.history()).isNotNull();
        assertThat(result.queueItems()).containsExactly(queue);
    }

    @Test
    @DisplayName("빈 큐이면 null을 반환한다")
    void process_withEmptyQueues_returnsNull() {
        // given
        Long sellerId = 1L;
        when(settlementQueueRepository.findAllReadyQueuesBySellerId(sellerId))
                .thenReturn(List.of());

        // when
        ExecutionResult result = settlementExecutionService.process(sellerId);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("write 호출 시 history 저장, 큐 상태 전이, 이벤트 발행이 수행된다")
    void write_savesHistoryAndCompletesQueues() {
        // given
        SettlementItem item = mock(SettlementItem.class);
        SettlementQueue queue = mock(SettlementQueue.class);
        when(queue.getItem()).thenReturn(item);

        Money amount = Money.of(10000);
        SettlementAmountSummary summary = new SettlementAmountSummary(
                amount, Money.of(500), Money.of(300), Money.of(9200)
        );
        SettlementHistory history = new SettlementHistory(1L, summary, 1);

        SettlementHistory savedHistory = mock(SettlementHistory.class);
        when(savedHistory.getId()).thenReturn(99L);
        when(savedHistory.getSellerId()).thenReturn(1L);
        when(savedHistory.getAmountSummary()).thenReturn(summary);
        when(settlementHistoryRepository.save(any(SettlementHistory.class))).thenReturn(savedHistory);

        ExecutionResult result = new ExecutionResult(history, List.of(queue));

        // when
        settlementExecutionService.write(result);

        // then
        verify(queue).startProcessing();
        verify(item).processing();
        verify(settlementHistoryRepository).save(history);
        verify(queue).done(99L);
        verify(settlementQueueRepository).saveAll(List.of(queue));
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("markAsFailed 호출 시 큐와 아이템이 실패 상태가 된다")
    void markAsFailed_setsFailAndFailedStatus() {
        // given
        SettlementItem item = mock(SettlementItem.class);
        SettlementQueue queue = mock(SettlementQueue.class);
        when(queue.getItem()).thenReturn(item);

        SettlementHistory history = mock(SettlementHistory.class);
        ExecutionResult result = new ExecutionResult(history, List.of(queue));

        // when
        settlementExecutionService.markAsFailed(result);

        // then
        verify(queue).fail();
        verify(item).failToExecute();
        verify(settlementQueueRepository).saveAll(List.of(queue));
    }

    @Test
    @DisplayName("markAsManual 호출 시 큐는 FAIL, 아이템은 MANUAL 상태가 된다")
    void markAsManual_setsFailAndManualStatus() {
        // given
        SettlementItem item = mock(SettlementItem.class);
        SettlementQueue queue = mock(SettlementQueue.class);
        when(queue.getItem()).thenReturn(item);

        SettlementHistory history = mock(SettlementHistory.class);
        ExecutionResult result = new ExecutionResult(history, List.of(queue));

        // when
        settlementExecutionService.markAsManual(result);

        // then
        verify(queue).fail();
        verify(item).manual();
        verify(settlementQueueRepository).saveAll(List.of(queue));
    }
}
