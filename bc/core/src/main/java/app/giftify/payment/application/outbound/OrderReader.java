package app.giftify.payment.application.outbound;

import java.util.Optional;

/**
 * Order BC 조회 인터페이스.
 * Payment BC에서 Order BC의 주문 정보를 읽어오기 위한 포트입니다.
 *
 * <p>구현체는 adapter 레이어에서 제공되며, 배포 환경에 따라 다양하게 구현될 수 있습니다:</p>
 * <ul>
 *   <li>모듈러 모놀리스: Order BC의 public port 직접 호출</li>
 *   <li>MSA (동기): REST API 호출</li>
 *   <li>MSA (비동기): 이벤트 기반 데이터 복제 후 로컬 조회</li>
 * </ul>
 */
public interface OrderReader {

	/**
	 * 주문 ID로 주문 정보를 조회합니다.
	 *
	 * @param orderId Order BC에서 발급한 주문 대체키
	 * @return 주문 정보 (없으면 empty)
	 */
	Optional<OrderInfo> findByOrderId(String orderId);
}