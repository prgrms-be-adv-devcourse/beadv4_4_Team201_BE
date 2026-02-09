package app.giftify.settlement.adapter.outbound.batch;

import app.giftify.settlement.application.outbound.port.SettlementItemRepository;
import app.giftify.settlement.domain.SettlementItemStatus;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@StepScope
public class OrderIdPartitioner implements Partitioner {

    @Value("${settlement.batch.order-per-partition}")
    private int orderPerPartition;

    private final SettlementItemRepository settlementItemRepository;

    @Value("#{jobParameters['cutOffDateTime']}")
    private LocalDateTime cutOffDateTime;

    @Value("#{jobParameters['retryLimit']}")
    private int retryLimit;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        List<Long> orderIds = settlementItemRepository.findPendingOrderIds(
                SettlementItemStatus.PENDING,
                cutOffDateTime,
                retryLimit
        );

        return partitionOrderIds(orderIds);
    }

    private @NonNull Map<String, ExecutionContext> partitionOrderIds(List<Long> orderIds) {
        Map<String, ExecutionContext> partitions = new LinkedHashMap<>();

        int partitionIndex = 0;
        for (int i = 0; i < orderIds.size(); i += orderPerPartition) {
            List<Long> partitionOrderIds = orderIds.subList(
                    i,
                    Math.min(i + orderPerPartition, orderIds.size())
            );

            ExecutionContext context = new ExecutionContext();
            context.put("orderIds", new ArrayList<>(partitionOrderIds));

            partitions.put("partition-" + partitionIndex++, context);
        }
        return partitions;
    }
}