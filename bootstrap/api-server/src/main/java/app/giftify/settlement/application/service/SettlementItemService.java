package app.giftify.settlement.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.settlement.application.inbound.CancelSettlementCommand;
import app.giftify.settlement.application.inbound.CreateSettlementCommand;
import app.giftify.settlement.application.outbound.port.SettlementItemRepository;
import app.giftify.settlement.application.service.dto.SettlementSummary;
import app.giftify.settlement.domain.model.SettlementCore;
import app.giftify.settlement.domain.model.SettlementItem;
import app.giftify.settlement.domain.model.SettlementItemType;
import app.giftify.settlement.domain.service.FeePolicyService;
import app.giftify.settlement.domain.service.SettlementCalculator;
import app.giftify.settlement.domain.snapshot.OrderItemSnapshot;
import app.giftify.settlement.domain.projection.AmountSummaryProjection;
import app.giftify.support.common.money.Money;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementItemService {
	private static final Logger log = LoggerFactory.getLogger(SettlementItemService.class);


    private final SettlementItemRepository settlementItemRepository;
    private final FeePolicyService feePolicyService;

    @Transactional
    public void create(CreateSettlementCommand command) {
        OrderItemSnapshot snapshot = command.snapshot();
        final SettlementItemType type = SettlementItemType.ITEM_PAYMENT;

        if (settlementItemRepository.existsByOrderItemIdAndType(snapshot.orderItemId(), type)) {
            log.warn("[중복 정산 아이템] 이미 존재하는 주문 아이템 ID, 정산 아이템 타입으로 저장을 스킵합니다. - OrderItemID: {}, Type: {}",
                    snapshot.orderItemId(), type);
            return;
        }

        SettlementItem item = SettlementItem.create(snapshot, getSettlementCore(snapshot.amount()));
        settlementItemRepository.save(item);
    }

    public Map<Long, Money> getTotalAmounts(List<Long> orderIds) {
        List<AmountSummaryProjection> projections = settlementItemRepository.getSettlementSumByOrderIds(orderIds);

        return projections.stream()
                .collect(Collectors.toMap(
                        AmountSummaryProjection::orderId,
                        p -> Money.of(p.totalAmount())
                ));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancel(CancelSettlementCommand command) {
        SettlementItem settlementItem = settlementItemRepository.getByOrderIdAndOrderItemIdAndTypeWithLock(
                command.orderId(), command.orderItemId(), SettlementItemType.ITEM_PAYMENT
        );

        settlementItem.cancel();
    }

    @Transactional(readOnly = true)
    public Page<SettlementSummary> summarizeSettlements(Long sellerId, Pageable pageable) {
        return settlementItemRepository.getSettlementSummary(sellerId, pageable);
    }

    private @NonNull SettlementCore getSettlementCore(Money amount) {
        BigDecimal platformFeeRate = feePolicyService.getPlatformFeeRate();
        BigDecimal pgFeeRate = feePolicyService.getPgFeeRate();
        return SettlementCalculator.calculate(amount, platformFeeRate, pgFeeRate);
    }
}
