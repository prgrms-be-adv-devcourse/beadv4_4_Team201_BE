package app.giftify.settlement.adapter.inbound.event;

import app.giftify.settlement.application.inbound.CancelSettlementCommand;
import app.giftify.settlement.application.inbound.CreateSettlementCommand;
import app.giftify.settlement.application.service.SettlementCancelService;
import app.giftify.settlement.application.service.SettlementItemService;
import app.giftify.settlement.domain.snapshot.OrderItemSnapshot;
import app.giftify.order.domain.event.OrderCanceledEvent;
import app.giftify.order.domain.event.OrderItemConfirmedEvent;
import app.giftify.order.domain.vo.CanceledItemSnapshot;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SettlementEventListener {

	private final SettlementItemService settlementItemService;
	private final SettlementCancelService settlementCancelService;

	@ApplicationModuleListener
	public void on(OrderItemConfirmedEvent event) {
		OrderItemSnapshot snapshot = OrderItemSnapshot.of(event);

		settlementItemService.create(new CreateSettlementCommand(snapshot));
	}

	@ApplicationModuleListener
	public void on(OrderCanceledEvent event) {
		List<Long> itemIds = getOrderItemIds(event);

		itemIds.forEach(itemId ->
				settlementCancelService.cancel(new CancelSettlementCommand(event.getOrderId(), itemId)));
	}

	private static @NonNull List<Long> getOrderItemIds(OrderCanceledEvent event) {
		return event.getItems().stream().map(CanceledItemSnapshot::orderItemId).toList();
	}
}
