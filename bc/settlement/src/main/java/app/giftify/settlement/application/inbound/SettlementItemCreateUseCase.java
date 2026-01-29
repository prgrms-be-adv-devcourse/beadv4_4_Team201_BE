package app.giftify.settlement.application.inbound;

public interface SettlementItemCreateUseCase {

    void createPaymentItem(CreatePaymentItemCommand command);
}
