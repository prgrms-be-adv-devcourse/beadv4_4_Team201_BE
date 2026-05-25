package app.giftify.settlement.adapter.outbound.batch.validation;

import app.giftify.settlement.adapter.outbound.batch.common.BatchProperties;
import app.giftify.settlement.adapter.outbound.client.OrderClient;
import app.giftify.settlement.adapter.outbound.client.PaymentClient;
import app.giftify.settlement.application.service.SettlementItemService;
import app.giftify.settlement.application.outbound.port.SettlementItemRepository;
import app.giftify.support.common.money.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ValidationBulkLoadListener implements StepExecutionListener {

    private final ValidationAmountContext cache;
    private final OrderClient orderClient;
    private final PaymentClient paymentClient;
    private final SettlementItemService settlementItemService;
    private final SettlementItemRepository settlementItemRepository;
    private final BatchProperties properties;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        Long minOrderId = (Long) stepExecution.getExecutionContext().get("minOrderId");
        Long maxOrderId = (Long) stepExecution.getExecutionContext().get("maxOrderId");

        List<Long> orderIds = settlementItemRepository.findDistinctOrderIdsBetween(
                minOrderId, maxOrderId, properties.retryLimit()
        );

        Map<Long, Money> orderAmounts = orderClient.getTotalAmounts(orderIds);
        Map<Long, Money> paymentAmounts = paymentClient.getTotalAmounts(orderIds);
        Map<Long, Money> settlementAmounts = settlementItemService.getTotalAmounts(orderIds);

        cache.loadOrderAmounts(orderAmounts);
        cache.loadPaymentAmounts(paymentAmounts);
        cache.loadSettlementAmounts(settlementAmounts);
    }
}
