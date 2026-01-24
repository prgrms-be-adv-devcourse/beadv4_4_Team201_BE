package app.giftify.payment.adapter.in.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import settlement.usecase.SettlementItemCreateUseCase;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementItemEventListener {

    private final SettlementItemCreateUseCase settlementItemCreateUseCase;

// todo: 주문 생성 이벤트 발행 요청

//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void handle(OrderItemCreatedEvent event) {
//        try {
//            CreatePaymentItemCommand command = new CreatePaymentItemCommand(
//                    event.getSellerId(),
//                    event.getOrderItemInfo()
//            );
//
//            settlementItemCreateUseCase.createPaymentItem(command);
//        } catch (Exception e) {
//            // todo: 재시도 로직과 예외 처리
//            log.error("[SETTLEMENT] Failed to create payment item for order item: {}", event.getOrderItemInfo().orderId(), e);
//        }
//    }
}
