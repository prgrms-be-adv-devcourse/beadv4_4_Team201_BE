package app.giftify.orderDemo.application;

import app.giftify.orderDemo.application.inbound.command.PlaceOrderForItemCommand;
import app.giftify.orderDemo.domain.OrderSnapshot;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    public OrderSnapshot placeOrderForItem(PlaceOrderForItemCommand command) {
        return null;
    }
}
