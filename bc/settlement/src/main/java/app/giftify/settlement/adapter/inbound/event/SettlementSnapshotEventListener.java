package app.giftify.settlement.adapter.inbound.event;

import app.giftify.settlement.application.service.SettlementItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementSnapshotEventListener {
	private final SettlementItemService settlementItemService;
}
