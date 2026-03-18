package app.giftify.notification.domain;

public enum NotificationType {
	FUNDING_CREATED("새로운 펀딩이 시작되었습니다"),
	FUNDING_ACHIEVED("펀딩이 완료되었습니다"),
	FUNDING_EXPIRED("펀딩이 만료되었습니다"),
	FUNDING_CANCELED("펀딩이 취소되었습니다"),
    FUNDING_FAIL_ACCEPT("펀딩 수락이 실패했습니다. 재수락 해주세요"),
	FRIEND_REQUEST_RECEIVED("친구 요청이 도착했습니다"),
	FRIEND_REQUEST_ACCEPTED("친구 요청이 수락되었습니다"),
	PAYMENT_SUCCEEDED("결제가 완료되었습니다"),
	PAYMENT_FAILED("결제에 실패했습니다"),
	PAYMENT_CANCEL_SUCCEEDED("결제 취소가 완료되었습니다"),
	PAYMENT_CANCEL_FAILED("결제 취소에 실패했습니다"),
    PRODUCT_SELLER_ORDER_RECEIVED("[판매자 알림] 새로운 주문이 인입되었습니다");


    private final String title;

    NotificationType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
