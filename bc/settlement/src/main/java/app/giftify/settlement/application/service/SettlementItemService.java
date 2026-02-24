package app.giftify.settlement.application.service;

import app.giftify.settlement.application.inbound.CreateSettlementItemCommand;
import app.giftify.settlement.application.outbound.port.SettlementItemRepository;
import app.giftify.settlement.domain.service.FeePolicyService;
import app.giftify.shared.api.AmountSummaryProjection;
import app.giftify.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final FeePolicyService feePolicyService;

    @Transactional
    public void create(CreateSettlementItemCommand command) {
    }

    public Map<Long, Money> getTotalAmounts(List<Long> orderIds) {
        List<AmountSummaryProjection> projections = settlementItemRepository.getSettlementSumByOrderIds(orderIds);

        return projections.stream()
                .collect(Collectors.toMap(
                        AmountSummaryProjection::orderId,
                        p -> Money.of(p.totalAmount())
                ));
    }
}
