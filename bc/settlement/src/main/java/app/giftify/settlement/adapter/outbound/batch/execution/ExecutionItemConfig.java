package app.giftify.settlement.adapter.outbound.batch.execution;

import app.giftify.settlement.application.SettlementExecutionService;
import app.giftify.settlement.application.outbound.port.SettlementHistoryRepository;
import app.giftify.settlement.application.outbound.port.SettlementQueueRepository;
import app.giftify.shared.domain.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class ExecutionItemConfig {

    private final SettlementExecutionService settlementExecutionService;
    private final SettlementQueueRepository settlementQueueRepository;
    private final SettlementHistoryRepository settlementHistoryRepository;
    private final EventPublisher eventPublisher;

    @Bean
    @StepScope
    public ListItemReader<Long> settlementQueueReader(
            @Value("#{stepExecutionContext['sellerIds']}") List<Long> sellerIds
    ) {
        return new ListItemReader<>(sellerIds);
    }

    @Bean
    @StepScope
    public SettlementExecutionProcessor settlementExecutionProcessor() {
        return new SettlementExecutionProcessor(settlementExecutionService);
    }

    @Bean
    public SettlementExecutionWriter settlementExecutionWriter() {
        return new SettlementExecutionWriter(
                settlementHistoryRepository,
                settlementQueueRepository,
                eventPublisher
        );
    }
}
