package app.giftify.settlement.adapter.outbound.batch.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.settlement.application.service.SettlementExecutionService;
import app.giftify.support.common.api.exception.InfraErrorCode;
import app.giftify.support.common.api.exception.InfraException;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

@RequiredArgsConstructor
public class SettlementExecutionWriter implements ItemWriter<ExecutionResult> {
	private static final Logger log = LoggerFactory.getLogger(SettlementExecutionWriter.class);


    private final SettlementExecutionService settlementExecutionService;

    @Override
    public void write(Chunk<? extends ExecutionResult> chunk) {
        for (ExecutionResult result : chunk) {
            try {
                settlementExecutionService.write(result);
            } catch (InfraException e) {
                InfraErrorCode errorCode = (InfraErrorCode) e.getErrorCode();
                if (errorCode.isRetryable()) {
                    settlementExecutionService.markAsFailed(result);
                } else {
                    settlementExecutionService.markAsManual(result);
                }

                log.warn("[{}] 정산 오류 발생 - sellerId: {}, settlementDate: {}. 처리상태: {}",
                        errorCode.getCode(),
                        result.history().getSellerId(),
                        result.history().getSettlementDate(),
                        errorCode.isRetryable() ? "FAILED" : "MANUAL", e);
            } catch (Exception e) {
                settlementExecutionService.markAsManual(result);

                log.warn("[UNKNOWN] 정산 오류 발생 - sellerId: {}, settlementDate: {}. 처리상태: MANUAL",
                        result.history().getSellerId(),
                        result.history().getSettlementDate(), e);
            }
        }
    }
}
