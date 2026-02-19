package app.giftify.funding.adpater.inbound;

import app.giftify.funding.application.WithdrawFundingUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelEventListener {
    private final WithdrawFundingUseCase withdrawFundingUseCase;

    // todo : 주문 취소 이벤트 구현 후 주석 제거
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void handle(OrderCancelEvent event) {
//        withdrawFundingUseCase.withdrawFunding(event.getFundingId(), event.getMemberId(), event.getMoney());
//    }
}
