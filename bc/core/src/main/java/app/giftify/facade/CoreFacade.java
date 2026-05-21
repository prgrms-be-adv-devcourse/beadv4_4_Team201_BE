package app.giftify.facade;

import app.giftify.funding.application.FundingFacade;
import app.giftify.order.application.OrderService;
import app.giftify.order.application.inbound.command.ParticipateFundingCommand;
import app.giftify.order.application.inbound.command.PlaceOrderCommand;
import app.giftify.order.application.inbound.vo.PlaceOrderResult;
import app.giftify.order.domain.OrderSnapshot;
import app.giftify.payment.application.CreatePaymentService;
import app.giftify.payment.application.inbound.CreatePaymentCommand;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.FundingSnapshot;
import app.giftify.shared.domain.vo.Money;
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

        CreatePaymentCommand paymentCommand = generatePaymentCommand(orderSnapshot, command.walletDeductAmount());
        createPaymentService.create(paymentCommand);

        return new PlaceOrderResult(orderSnapshot.orderId());
    }

    // FIXME: PaymentType을 OrderSnapshot의 주문 항목 타입(OrderItemType)으로부터 결정하도록 변경 필요
    //        현재는 participateFunding() 전용이라 FUNDING 하드코딩. 일반 상품 구매 추가 시 수정.
    private static @NonNull CreatePaymentCommand generatePaymentCommand(
            OrderSnapshot orderSnapshot, Money walletDeductAmount
    ) {
        if (walletDeductAmount.isGreaterThan(Money.zero())) {
            return CreatePaymentCommand.withWalletDeduct(
                    orderSnapshot.buyerId(),
                    orderSnapshot.orderId(),
                    orderSnapshot.orderNumber(),
                    PaymentType.FUNDING,
                    orderSnapshot.paymentMethod(),
                    orderSnapshot.totalAmount(),
                    walletDeductAmount
            );
        }
        return CreatePaymentCommand.of(
                orderSnapshot.buyerId(),
                orderSnapshot.orderId(),
                orderSnapshot.orderNumber(),
                PaymentType.FUNDING,
                orderSnapshot.paymentMethod(),
                orderSnapshot.totalAmount()
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
