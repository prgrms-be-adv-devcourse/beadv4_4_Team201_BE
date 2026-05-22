package app.giftify.order.application;

import app.giftify.order.application.dto.OrderCancelProcessingResult;
import app.giftify.order.application.dto.OrderItemCancelResult;
import app.giftify.order.domain.CancelTargetItems;
import app.giftify.order.domain.OrderItemStatus;
import app.giftify.order.domain.ResultCode;
import app.giftify.order.domain.OrderItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderCancelProcessor {
    public OrderCancelProcessingResult process(List<OrderItem> requestedItems) {
        List<OrderItemCancelResult> results = new ArrayList<>();
        List<OrderItem> pendingItems = new ArrayList<>();

        for (OrderItem item : requestedItems) {
            ResultCode resultCode = item.decideCancelResult();

            if (item.getStatus() == OrderItemStatus.CREATED) {
                item.canceled();
            }
            if (item.getStatus() == OrderItemStatus.PAID) {
                item.canceling();
                pendingItems.add(item);
            }

            results.add(new OrderItemCancelResult(
                    item.getId(),
                    item.getSellerId(),
                    item.getAmount(),
                    resultCode
            ));
        }

        return new OrderCancelProcessingResult(results, new CancelTargetItems(pendingItems));
    }
}
