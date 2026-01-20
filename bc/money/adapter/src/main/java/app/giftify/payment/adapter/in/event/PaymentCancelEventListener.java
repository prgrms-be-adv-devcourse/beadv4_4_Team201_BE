package app.giftify.payment.adapter.in.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import domain.payment.CancelReason;
import payment.usecase.PaymentCancelUseCase;
import payment.usecase.command.CancelPaymentCommand;

/**
 * 결제 프로세스를 취소하는 여러가지 이벤트를 수신하는 리스너
 */
@Component
public class PaymentCancelEventListener {
	private final PaymentCancelUseCase paymentCancelUseCase;

	public PaymentCancelEventListener(PaymentCancelUseCase paymentCancelUseCase) {
		this.paymentCancelUseCase = paymentCancelUseCase;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // FIXME :: 구체적인 펀딩쪽 이벤트 받아서 결제 취소 때리도록 마저 구현 필요
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handle(Object fundingCanceledEvent) {
		CancelPaymentCommand command = new CancelPaymentCommand(
			null, //fundingCanceledEvent.getPaymentId(),
			null, // 시스템에 의한 취소이므로 요청자 ID는 null 혹은 시스템 ID
			CancelReason.FUNDING_FAILED
		);
		paymentCancelUseCase.cancel(command);
	}
}
