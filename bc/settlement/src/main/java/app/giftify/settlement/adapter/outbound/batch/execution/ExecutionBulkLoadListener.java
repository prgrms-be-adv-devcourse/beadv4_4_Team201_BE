package app.giftify.settlement.adapter.outbound.batch.execution;

import app.giftify.settlement.application.outbound.port.SettlementQueueRepository;
import app.giftify.settlement.domain.status.SettlementQueueStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExecutionBulkLoadListener implements StepExecutionListener {

    private final SettlementQueueRepository settlementQueueRepository;
    private static final String SELLER_IDS_KEY = "sellerIds";

    @Override
    public void beforeStep(StepExecution stepExecution) {
        List<Long> sellerIds = settlementQueueRepository.findDistinctSellerIdsByStatus(SettlementQueueStatus.READY);

        ExecutionContext context = stepExecution.getExecutionContext();
        context.put(SELLER_IDS_KEY, new ArrayList<>(sellerIds));

        log.info("정산 대상 판매자 로드 완료. 건수: {}", sellerIds.size());
    }
}
