package app.giftify.payment.adapter.inbound.event;

import org.springframework.stereotype.Component;

import app.giftify.payment.application.inbound.CancelPaymentUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelEventListener {

	private final CancelPaymentUseCase cancelPaymentUseCase;

	// TODO: FundingCanceledEvent → Payment 연결 경로 부재
	//  현재 FundingCanceledEvent는 fundingId, wishlistItemId, canceledAmount만 포함.
	//  Payment를 찾으려면 orderId가 필요하지만, Funding 엔티티에 orderId가 없음.
	//   1. Funding 엔티티에 orderId 저장 → FundingCanceledEvent에 포함
	//   2.  Funding → Order 매핑 테이블 도입
	//  결정 후 @ApplicationModuleListener로 전환 필요.
	//
	// @ApplicationModuleListener
	// public void handle(FundingCanceledEvent event) {
	//     String orderId = ???; // Funding → orderId 매핑 필요
	//     paymentRepository.findByOrderId(orderId).stream()
	//         .filter(Payment::isCancelable)
	//         .forEach(payment -> cancelPaymentUseCase.cancel(
	//             new CancelPaymentCommand(payment.getId(), SYSTEM_REQUESTER_ID, "펀딩 취소에 의한 자동 취소")
	//         ));
	// }
}
