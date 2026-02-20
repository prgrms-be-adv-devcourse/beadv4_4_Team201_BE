package app.giftify.wallet.adapter.inbound.event;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.settlement.SettlementCreatedEvent;
import app.giftify.shared.domain.event.wallet.WalletPayoutFailedEvent;
import app.giftify.wallet.application.inbound.SettlementPayoutCommand;
import app.giftify.wallet.application.inbound.SettlementPayoutUseCase;
import app.giftify.wallet.domain.WalletException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementCreatedEventListener {
	private final SettlementPayoutUseCase settlementPayoutUseCase;
	private final EventPublisher eventPublisher;

	// 지갑은 해당 이벤트를 수신해 지갑 이벤트 내에 있는 데이터로 정산금 지급/차감
	// 지급 차갑의 기준은 이벤트 내부의 정산금 필드의 부호값
	// 지급에 실패할 시, 정산 이력과 정산 상태 변경이 롤백 되어야 함
	// Spring Modulith에서 지원하는 아웃박스를 활용해 정산 실행 원자성 보장
	// 지급, 차감을 위한 시스템 지갑이 필요할 수 있음
	@ApplicationModuleListener // FIXME :: 정산이 외부로 분리되면 리스너 로직 변경되어야 함
	public void handle(SettlementCreatedEvent event) {
		try {
			log.info("[SettlementCreatedEventListener] 정산 이벤트 수신. settlementId={}, sellerId={}, amount={}",
				event.getSettlementId(), event.getSellerId(), event.getTotalAmount());
			SettlementPayoutCommand command = new SettlementPayoutCommand(
				event.getSettlementId(),
				event.getSellerId(),
				event.getTotalAmount(),
				event.getEventId()
			);
			settlementPayoutUseCase.payout(command);
		} catch (WalletException e) {
			log.error("[SettlementCreatedEventListener] 정산 지급 재시도 불가 실패. settlementId={}, reason={}",
				event.getSettlementId(), e.getMessage()
			);

			eventPublisher.publish(new WalletPayoutFailedEvent(
				event.getSettlementId(),
				event.getSellerId(),
				event.getTotalAmount(),
				e.getMessage()
			));
		}
	}
}
