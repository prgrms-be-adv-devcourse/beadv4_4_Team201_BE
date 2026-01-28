package app.giftify.funding.domain.product;

public enum StockChangeType {
	MANUAL_ADJUST, // 판매자 수동 수정
	ORDER_DEDUCT, // 펀딩 성공으로 차감
	ORDER_RESTORE // 펀딩 취소/환불 복구
}
