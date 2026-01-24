package settlement;

import domain.settlement.SettlementItem;
import domain.settlement.SettlementItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settlement.command.CreatePaymentItemCommand;
import settlement.usecase.SettlementItemCreateUseCase;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementItemService implements SettlementItemCreateUseCase {

    private final SettlementItemRepository settlementItemRepository;

    @Override
    @Transactional
    public void createPaymentItem(CreatePaymentItemCommand command) {
        Long orderItemId = command.orderItemInfo().orderItemId();

        if (settlementItemRepository.existsByOrderItemId(orderItemId)) {
            log.info("[SETTLEMENT] 중복 요청 - 이미 존재함. orderItemId: {}", orderItemId);
            return;
        }

        try {
            SettlementItem settlementItem = SettlementItem.createPaymentItem(command.orderItemInfo());

            // 중복이 있다면 이 자리에서 예외가 발생하도록 saveAndFlush 호출
            settlementItemRepository.saveAndFlush(settlementItem);

            log.info("[SETTLEMENT] 정산 아이템 생성 완료: orderItemId={}, status={}",
                    orderItemId, settlementItem.getStatus());

        } catch (DataIntegrityViolationException e) {
            // 동시성 이슈로 인해 유니크 제약 조건 위반 시 로그만 남기고 정상 진행(멱등성 보장)
            log.warn("[SETTLEMENT] 동시성 이슈 - 이미 처리 중인 정산 아이템입니다. orderItemId: {}", orderItemId);
        }
        // DB 연결 끊김 등의 다른 RuntimeException은 그대로 밖으로 던져짐 -> 리스너 전체 롤백
    }
}