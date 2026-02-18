package app.giftify.settlement.adapter.outbound.batch.execution;

import app.giftify.settlement.application.SettlementExecutionService;
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
        return new SettlementExecutionWriter(settlementExecutionService);
    }
}
