package app.giftify.payment.adapter.out.jpa.repository.payment; // FIXME : 프로젝트 초기에 모듈이랑 패키지명 payment 라고 했던걸 안 바꾸고 그대로 사용중입니다... money 로 변경 필요함

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.payment.adapter.out.jpa.entity.payment.JpaPaymentHistory;
import domain.payment.PaymentEventType;

public interface JpaPaymentHistoryRepository extends JpaRepository<JpaPaymentHistory, Long> {

	/**
	 * @param paymentId  paymentId
	 * @return 모든 이력 조회 (시간순 정렬)
	 */
	List<JpaPaymentHistory> findByPaymentIdOrderByOccurredAtAsc(Long paymentId);

	/**
	 * @param paymentId paymentId
	 * @param eventType 결제 이벤트 타입
	 * @return paymentId + eventType으로 특정 결제 이벤트 조회
	 */
	Optional<JpaPaymentHistory> findByPaymentIdAndEventType(Long paymentId, PaymentEventType eventType);
}
