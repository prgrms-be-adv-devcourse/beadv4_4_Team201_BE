package settlement.usecase;

import settlement.command.CreatePaymentItemCommand;

public interface SettlementItemCreateUseCase {

    void createPaymentItem(CreatePaymentItemCommand command);
}
