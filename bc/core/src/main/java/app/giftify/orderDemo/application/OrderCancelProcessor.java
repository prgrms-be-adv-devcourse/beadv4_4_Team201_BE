package app.giftify.orderDemo.application;

import app.giftify.orderDemo.application.dto.OrderCancelProcessingResult;
import app.giftify.orderDemo.domain.CancelTargetItems;
import app.giftify.orderDemo.domain.OrderCancelResultCode;
import app.giftify.orderDemo.application.dto.OrderItemCancelResult;
import app.giftify.orderDemo.domain.OrderItem;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderCancelProcessor {
    public OrderCancelProcessingResult process(List<OrderItem> requestedItems) {
        List<OrderItemCancelResult> results = new ArrayList<>();
        List<OrderItem> pendingItems = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (OrderItem item : requestedItems) {
            OrderCancelResultCode resultCode = OrderCancelResultCode.determineCancelResultCode(item.getStatus());

            if (resultCode == OrderCancelResultCode.SUCCESS) {
                item.cancel(now);
            } else if (resultCode == OrderCancelResultCode.ACCEPTED) {
                item.pendingToCancel(now);
                pendingItems.add(item);
            }

            results.add(new OrderItemCancelResult(item.getId(), item.getSellerId(), item.getAmount(), resultCode));
        }

        return new OrderCancelProcessingResult(
                results,
                new CancelTargetItems(pendingItems)
        );
    }
}