package app.giftify.payment.adapter.inbound.event;

import static app.giftify.payment.domain.SystemConstants.SYSTEM_REQUESTER_ID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import app.giftify.payment.application.inbound.CancelPaymentCommand;
import app.giftify.payment.application.inbound.CancelPaymentUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 취소 이벤트 리스너.
 * Funding BC 등에서 발행하는 취소 이벤트를 수신합니다.
 *
 * TODO: 구체적인 이벤트 타입 정의 후 구현 완료 필요
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelEventListener {

	private final CancelPaymentUseCase cancelPaymentUseCase;

	// TODO: FundingCanceledEvent 등 구체적 이벤트 정의 후 활성화
	// @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	// public void handle(FundingCanceledEvent event) {
	//     CancelPaymentCommand command = new CancelPaymentCommand(
	//         event.getPaymentId(),
	//         SYSTEM_REQUESTER_ID,
	//         "FUNDING_CANCELED"
	//     );
	//     cancelPaymentUseCase.cancel(command);
	// }
}
