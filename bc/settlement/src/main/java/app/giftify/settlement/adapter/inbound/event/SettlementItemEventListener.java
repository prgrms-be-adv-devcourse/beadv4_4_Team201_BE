package app.giftify.settlement.adapter.inbound.event;

import app.giftify.settlement.application.inbound.SettlementItemCreateUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
//            for (OrderItemDetail item : event.items()) {
//                OrderItemInfo itemInfo = new OrderItemInfo(
//                        event.getOrderId(),
//                        event.getOrderNumber(),
//                        item.getOrderItemId(),
//                        item.getSellerId(),
//                        item.getQuantity(),
//                        item.totalAmount(),
//                        event.getOrderedAt()
//
//                );
//                CreatePaymentItemCommand command = new CreatePaymentItemCommand(itemInfo);
//
//                settlementItemCreateUseCase.createPaymentItem(command);
//            }
//        } catch (DataIntegrityViolationException e) {
//            // 서비스에서 이미 처리했지만, 혹시 리스너 레벨에서 터질 경우를 대비한 방어
//            log.warn("[SETTLEMENT] 동시성 이슈 - 이미 처리 중인 정산 아이템입니다. orderItemId: {}", orderItemId);
//        } catch (IllegalArgumentException | IllegalStateException e) {
//            // 도메인 예외: 데이터 자체가 잘못됨 -> 롤백 후 재시도 '하지 않음' (에러 로그 및 알림)
//            log.error(""[SETTLEMENT] 비즈니스 로직 오류 - 데이터 확인 필요: {}", event.getOrderId(), e);
//            // throw e; 를 하지 않거나, Retry 대상에서 제외
//        } catch (Exception e) {
//            // 시스템 예외(DB 장애 등): 롤백 후 '재시도 수행'
//            log.error(""[SETTLEMENT] 시스템 장애 발생 - 재시도 예정: {}", event.getOrderId(), e);
//            throw e;
//        }
//    }
}
