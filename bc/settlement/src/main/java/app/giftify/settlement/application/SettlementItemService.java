package app.giftify.settlement.application;

import app.giftify.settlement.application.inbound.InitializeSettlementItemCommand;
import app.giftify.settlement.application.outbound.port.OrderItemSnapshotRepository;
import app.giftify.settlement.application.outbound.port.OrderSnapshotRepository;
import app.giftify.settlement.application.outbound.port.PaymentSnapshotRepository;
import app.giftify.settlement.application.outbound.port.SettlementItemRepository;
import app.giftify.settlement.domain.*;
import app.giftify.settlement.domain.exception.InfraException;
import app.giftify.shared.api.AmountSummaryProjection;
import app.giftify.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementItemService {

    private final SettlementItemRepository settlementItemRepository;
    private final OrderSnapshotRepository orderSnapshotRepository;
    private final OrderItemSnapshotRepository orderItemSnapshotRepository;
    private final PaymentSnapshotRepository paymentSnapshotRepository;
    private final FeePolicyService feePolicyService;

    @Retryable(
            value = { InfraException.class }, // 재시도 대상 예외
            maxAttempts = 3,                  // 최대 3회
            backoff = @Backoff(delay = 10000, multiplier = 2)// 10초 딜레이
    )
    @Transactional
    public void initializeSettlementItem(InitializeSettlementItemCommand command) {
        SettlementSource source = fetchSettlementSource(command);

        SettlementCore core = SettlementCalculator.calculate(
                source.getPaidAmount(),
                feePolicyService.getPlatformFeeRate(),
                feePolicyService.getPgFeeRate()
        );

        SettlementItem paymentItem = SettlementItem.createPaymentItem(
                source,
                core,
                command.confirmedAt()
        );

        settlementItemRepository.save(paymentItem);
    }

    @Recover
    public void recover(InfraException e, InitializeSettlementItemCommand command) {
        log.error("[SettlementItemService] 재시도 실패, fundingId={}, message={}",
                command.fundingId(), e.getMessage(), e);
    }

    public Map<Long, Money> getTotalAmounts(List<Long> orderIds) {
        List<AmountSummaryProjection> projections = settlementItemRepository.getSettlementSumByOrderIds(orderIds);

        return projections.stream()
                .collect(Collectors.toMap(
                        AmountSummaryProjection::orderId,
                        p -> Money.of(p.totalAmount())
                ));
    }

    private SettlementSource fetchSettlementSource(InitializeSettlementItemCommand command) {
        OrderItemSnapshot item = orderItemSnapshotRepository.getByTargetId(command.fundingId());
        OrderSnapshot order = orderSnapshotRepository.getById(item.getOrderId());
        PaymentSnapshot payment = paymentSnapshotRepository.getByOrderNumber(order.getOrderNumber());

        return new SettlementSource(item, order, payment);
    }
}
