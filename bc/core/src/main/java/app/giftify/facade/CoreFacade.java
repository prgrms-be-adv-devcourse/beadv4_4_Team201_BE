package app.giftify.facade;

import app.giftify.facade.command.ParticipateFundingCommand;
import app.giftify.facade.mapper.OrderItemSnapshotMapper;
import app.giftify.facade.vo.PlaceOrderResult;
import app.giftify.funding.application.FundingFacade;
import app.giftify.order.application.OrderService;
import app.giftify.order.application.inbound.command.PlaceOrderCommand;
import app.giftify.order.application.inbound.command.MarkOrderAsPaidCommand;
import app.giftify.order.domain.OrderSnapshot;
import app.giftify.payment.application.CreatePaymentService;
import app.giftify.payment.application.inbound.CreateFundingPaymentCommand;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.shared.domain.vo.FundingSnapshot;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CoreFacade {

    private final OrderService orderService;
    private final FundingFacade fundingFacade;
    private final CreatePaymentService createPaymentService;

    /**
     * 트랜잭션 하나로 묶고
     * 실패하면 전체 롤백
     */
    @Transactional
    public PlaceOrderResult participateFunding(ParticipateFundingCommand command) {
        OrderSnapshot orderSnapshot = orderService.createOrder(PlaceOrderCommand.of(command));

        CreateFundingPaymentCommand paymentCommand = generatePaymentCommand(orderSnapshot);
        PaymentCreatedResult paymentResult = createPaymentService.create(paymentCommand);

        MarkOrderAsPaidCommand markOrderAsPaidCommand = new MarkOrderAsPaidCommand(
                paymentResult.orderNumber(),
                paymentResult.paymentId(),
                paymentResult.lastTransactionKey(),
                paymentResult.createdAt()
        );
        orderService.markOrderAsPaid(markOrderAsPaidCommand);

        fundingFacade.processFundingActions(orderSnapshot);

        return new PlaceOrderResult(orderSnapshot.orderId());
    }

    private static @NonNull CreateFundingPaymentCommand generatePaymentCommand(OrderSnapshot orderSnapshot) {
        return new CreateFundingPaymentCommand(
                orderSnapshot.buyerId(),
                orderSnapshot.orderId(),
                orderSnapshot.orderNumber(),
                orderSnapshot.paymentMethod(),
                orderSnapshot.totalAmount(),
                OrderItemSnapshotMapper.fromOrderList(orderSnapshot.orderItemSnapshots())
        );
    }

    // todo: FundingFacade에서 List로 반환하도록 수정 시 제거 예정
    private @NonNull List<FundingSnapshot> getFundingSnapshots(ParticipateFundingCommand command) {
        return command.items().stream()
                .map(itemRequest -> fundingFacade.getSnapshot(itemRequest.wishlistItemId())) // Optional<FundingSnapshot> 반환
                .flatMap(Optional::stream) // 값이 있는 것만 꺼내고 빈 것은 제거
                .toList();
    }

}
