package app.giftify.settlement.adapter.outbound.batch;

import app.giftify.settlement.adapter.outbound.client.OrderClient;
import app.giftify.settlement.adapter.outbound.client.PaymentClient;
import app.giftify.settlement.application.SettlementItemService;
import app.giftify.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
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

    @Override
    public void beforeStep(StepExecution stepExecution) {
        List<Long> orderIds = (List<Long>) stepExecution.getExecutionContext().get("orderIds");

        Map<Long, Money> orderAmounts = orderClient.getTotalAmounts(orderIds);
        Map<Long, Money> paymentAmounts = paymentClient.getTotalAmounts(orderIds);
        Map<Long, Money> settlementAmounts = settlementItemService.getTotalAmounts(orderIds);

        cache.loadOrderAmounts(orderAmounts);
        cache.loadPaymentAmounts(paymentAmounts);
        cache.loadSettlementAmounts(settlementAmounts);
    }
}