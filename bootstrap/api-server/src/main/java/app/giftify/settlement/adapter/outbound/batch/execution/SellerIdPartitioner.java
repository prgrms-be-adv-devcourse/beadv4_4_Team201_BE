package app.giftify.settlement.adapter.outbound.batch.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.settlement.adapter.outbound.batch.common.BatchProperties;
import app.giftify.settlement.application.outbound.port.SettlementQueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Component
@StepScope
@RequiredArgsConstructor
public class SellerIdPartitioner implements Partitioner {
	private static final Logger log = LoggerFactory.getLogger(SellerIdPartitioner.class);


    private final SettlementQueueRepository repository;
    private final BatchProperties properties;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        List<Long> sellerIds = repository.findDistinctSellerIdsByStatusReady();

        log.info("정산 대상 판매자 로드 완료. 건수: {}", sellerIds.size());

        if (CollectionUtils.isEmpty(sellerIds)) {
            return Map.of();
        }

        // 파티션 순서를 보장하기 위해 LinkedHashMap 사용
        Map<String, ExecutionContext> partitions = new LinkedHashMap<>();

        int partitionCount = (int) Math.ceil((double) sellerIds.size() / properties.chunkSize());

        for (int i = 0; i < partitionCount; i++) {
            int start = i * properties.chunkSize();
            int end = Math.min(start + properties.chunkSize(), sellerIds.size());

            List<Long> subIds = new ArrayList<>(sellerIds.subList(start, end));

            ExecutionContext context = new ExecutionContext();
            context.put("sellerIds", subIds);

            partitions.put("partition-e-" + i, context);
        }

        return partitions;
    }
}
