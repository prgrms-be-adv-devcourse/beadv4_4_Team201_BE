package app.giftify.usecase;

import app.giftify.order.application.OrderService;
import app.giftify.order.application.inbound.ParticipateFundingUseCase;
import app.giftify.order.application.inbound.command.ParticipateFundingCommand;
import app.giftify.order.application.inbound.command.PlaceOrderCommand;
import app.giftify.order.application.inbound.vo.PlaceOrderResult;
import app.giftify.order.domain.OrderSnapshot;
import app.giftify.payment.application.CreatePaymentService;
import app.giftify.payment.application.inbound.CreatePaymentCommand;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ParticipateFundingUseCaseService implements ParticipateFundingUseCase {

    private final OrderService orderService;
    private final CreatePaymentService createPaymentService;

    public ParticipateFundingUseCaseService(OrderService orderService, CreatePaymentService createPaymentService) {
        this.orderService = orderService;
        this.createPaymentService = createPaymentService;
    }

    @Override
    public PlaceOrderResult participateFunding(ParticipateFundingCommand command) {
        OrderSnapshot snapshot = orderService.createOrder(PlaceOrderCommand.of(command));
        createPaymentService.create(generatePaymentCommand(snapshot, command.walletDeductAmount()));
        return new PlaceOrderResult(snapshot.orderId());
    }

    private static @NonNull CreatePaymentCommand generatePaymentCommand(
            OrderSnapshot snapshot, Money walletDeductAmount
    ) {
        if (walletDeductAmount.isGreaterThan(Money.zero())) {
            return CreatePaymentCommand.withWalletDeduct(
                    snapshot.buyerId(),
                    snapshot.orderId(),
                    snapshot.orderNumber(),
                    PaymentType.FUNDING,
                    snapshot.paymentMethod(),
                    snapshot.totalAmount(),
                    walletDeductAmount
            );
        }
        return CreatePaymentCommand.of(
                snapshot.buyerId(),
                snapshot.orderId(),
                snapshot.orderNumber(),
                PaymentType.FUNDING,
                snapshot.paymentMethod(),
                snapshot.totalAmount()
        );
    }
}
