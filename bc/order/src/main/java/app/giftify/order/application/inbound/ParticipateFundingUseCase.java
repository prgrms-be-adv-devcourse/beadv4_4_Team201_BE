package app.giftify.order.application.inbound;

import app.giftify.order.application.inbound.command.ParticipateFundingCommand;
import app.giftify.order.application.inbound.vo.PlaceOrderResult;

public interface ParticipateFundingUseCase {

    PlaceOrderResult participateFunding(ParticipateFundingCommand command);
}
