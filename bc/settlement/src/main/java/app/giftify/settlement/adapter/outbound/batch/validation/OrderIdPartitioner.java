package app.giftify.settlement.adapter.outbound.batch.validation;

import app.giftify.settlement.adapter.outbound.batch.common.BatchProperties;
import app.giftify.settlement.application.outbound.port.SettlementItemRepository;
import app.giftify.settlement.domain.status.SettlementItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@StepScope
public class OrderIdPartitioner implements Partitioner {

    private final SettlementItemRepository settlementItemRepository;
    private final BatchProperties properties;

    @Value("#{jobParameters['cutOffDateTime']}")
    private LocalDateTime cutOffDateTime;

    @Value("#{jobParameters['retryLimit']}")
    private int retryLimit;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Long minOrderId = settlementItemRepository.getMinOrderId(
                SettlementItemStatus.PENDING, cutOffDateTime, retryLimit
        );
        Long maxOrderId = settlementItemRepository.getMaxOrderId(
                SettlementItemStatus.PENDING, cutOffDateTime, retryLimit
        );

        Map<String, ExecutionContext> partitions = new LinkedHashMap<>();
        if (minOrderId == null || maxOrderId == null) {
            return partitions; // 처리할 데이터 없음
        }

        long fromId = minOrderId;
        int partitionIndex = 0;

        while (fromId <= maxOrderId) {
            long toId = Math.min(fromId + properties.orderPerPartition() - 1, maxOrderId);

            ExecutionContext context = new ExecutionContext();
            context.putLong("minOrderId", fromId);
            context.putLong("maxOrderId", toId);

            partitions.put("partition-v-" + partitionIndex++, context);
            fromId = toId + 1;
        }

        return partitions;
    }
}