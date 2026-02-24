package app.giftify.settlement.application.service;

import app.giftify.settlement.application.inbound.CancelSettlementCommand;
import app.giftify.shared.api.exception.BusinessException;
import app.giftify.shared.api.exception.InfraException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementCancelService {

    private final SettlementItemService settlementItemService;

    @Retryable(
            retryFor = InfraException.class,
            exceptionExpression = "getErrorCode().isRetryable()",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 3000)
    )
    public void cancel(CancelSettlementCommand command) {
        Long orderId = command.orderId();
        Long itemId = command.orderItemId();

        try {
            settlementItemService.cancel(command);
        } catch (BusinessException e) {
            log.error("[정산 상태 업데이트] 비즈니스 예외 발생으로 주문 취소 반영에 실패했습니다. OrderID: {}, OrderItemID: {}, ErrorCode: {}",
                    orderId, itemId, e.getErrorCode().getCode(), e);
        } catch (InfraException e) {
            if (e.getErrorCode().isRetryable()) throw e;
            log.error("[정산 상태 업데이트] 인프라 예외 발생으로 주문 취소 반영에 실패했습니다. OrderID: {}, OrderItemID: {}, ErrorCode: {}",
                    orderId, itemId, e.getErrorCode().getCode(), e);
        } catch (Exception e) {
            log.error("[정산 상태 업데이트] 알 수 없는 예외 발생으로 주문 취소 반영에 실패했습니다. OrderID: {}, OrderItemID: {}",
                    orderId, itemId, e);
        }
    }

    @Recover
    public void recover(InfraException e, CancelSettlementCommand command) {
        log.error("[정산 상태 업데이트] 재시도 횟수를 초과했습니다. OrderID: {}, OrderItemID: {}, ErrorCode: {}",
                command.orderId(), command.orderItemId(), e.getErrorCode(), e);
    }
}
