package app.giftify.wallet.adapter.inbound.event;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import app.giftify.payment.domain.event.PaymentConfirmedEvent;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.wallet.application.inbound.ChargeWalletCommand;
import app.giftify.wallet.application.inbound.ChargeWalletUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Payment 결제 완료 이벤트를 수신하여 Wallet 충전을 처리하는 핸들러.
 * <p>
 * PaymentType.POINT_CHARGE 타입의 결제가 완료되면
 * WalletService.charge()를 호출하여 지갑 잔액을 증가시킵니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConfirmedEvnetHandler {
	private final ChargeWalletUseCase chargeWalletUseCase;

	/**
	 * ApplicationModuleListener 사용 이유:
	 * - Payment 트랜잭션 커밋 후 실행 (AFTER_COMMIT)
	 * - 실패 시 Event Publication에 기록되어 재시도 가능
	 * - Toss PG 승인을 롤백시켜야 하는 동기 트랜잭션 방식을 피하기 위해 사용
	 */
	@ApplicationModuleListener
	public void handle(PaymentConfirmedEvent event) {
		if (event.getPaymentType() != PaymentType.POINT_CHARGE) {
			return;
		}

		log.info("[PaymentConfirmedEvnetHandler] POINT_CHARGE 결제 완료. memberId={}, orderId={}, amount={}",
			event.getMemberId(), event.getOrderId(), event.getPaidAmount());

		ChargeWalletCommand command = new ChargeWalletCommand(
			event.getMemberId(),
			event.getPaidAmount(),
			event.getOrderId()
		);

		chargeWalletUseCase.charge(command);

		log.info("[PaymentConfirmedEvnetHandler] 지갑 충전 완료. memberId={}, amount={}",
			event.getMemberId(), event.getPaidAmount());
	}
}
