package app.giftify.settlement.application.service;

import app.giftify.settlement.application.dto.SettlementSource;
import app.giftify.settlement.application.inbound.InitializeSettlementItemCommand;
import app.giftify.settlement.application.outbound.port.OrderItemSnapshotRepository;
import app.giftify.settlement.application.outbound.port.OrderSnapshotRepository;
import app.giftify.settlement.application.outbound.port.PaymentSnapshotRepository;
import app.giftify.settlement.application.outbound.port.SettlementItemRepository;
import app.giftify.settlement.domain.model.SettlementCore;
import app.giftify.settlement.domain.model.SettlementItem;
import app.giftify.settlement.domain.service.FeePolicyService;
import app.giftify.settlement.domain.service.SettlementCalculator;
import app.giftify.settlement.domain.snapshot.OrderItemSnapshot;
import app.giftify.settlement.domain.snapshot.OrderSnapshot;
import app.giftify.settlement.domain.snapshot.PaymentSnapshot;
import app.giftify.shared.api.AmountSummaryProjection;
import app.giftify.shared.api.exception.InfraException;
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
            retryFor = InfraException.class,
            backoff = @Backoff(delay = 1000, multiplier = 2)
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
