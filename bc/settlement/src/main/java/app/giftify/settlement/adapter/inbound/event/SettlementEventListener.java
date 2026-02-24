package app.giftify.settlement.adapter.inbound.event;

import app.giftify.settlement.application.inbound.CreateSettlementItemCommand;
import app.giftify.settlement.application.service.SettlementItemService;
import app.giftify.settlement.domain.snapshot.OrderItemSnapshot;
import app.giftify.shared.domain.event.order.OrderItemConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementEventListener {

	private final SettlementItemService settlementItemService;

	@ApplicationModuleListener
	public void on(OrderItemConfirmedEvent event) {
		OrderItemSnapshot snapshot = OrderItemSnapshot.of(event);

		settlementItemService.create(new CreateSettlementItemCommand(snapshot));
	}
}
