package app.giftify.shared.domain.event.payment;

// 이거 여기서 정책 검증하면 망한다
// FIXME
// 첫번째, 이벤트랑 같이 묶어두면 안되는 enum 인데 같은 폴더에 묶어 놓음
public enum PaymentType {
	FUNDING,    // 펀딩 참여 결제
	CHARGE,
}
