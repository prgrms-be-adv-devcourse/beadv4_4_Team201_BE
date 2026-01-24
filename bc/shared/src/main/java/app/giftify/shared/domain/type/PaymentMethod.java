package app.giftify.shared.domain.type;

public enum PaymentMethod {
    CARD("카드 결제", "신용카드 / 체크카드"),

    KAKAO_PAY("카카오페이", "카카오페이 간편결제"),
    NAVER_PAY("네이버페이", "네이버페이 간편결제"),
    TOSS_PAY("토스페이", "토스 간편결제"),

    ACCOUNT_TRANSFER("계좌이체", "실시간 계좌이체"),
    VIRTUAL_ACCOUNT("가상계좌", "가상계좌 입금"),
    BANK_TRANSFER("무통장 입금", "은행 창구 또는 ATM 무통장 입금"),

    DEPOSIT("예치금 결제", "서비스 내부 예치금 사용"),
    POINT("포인트 결제", "서비스 내부 포인트 사용");

    private final String description;
    private final String detail;

    PaymentMethod(String description, String detail) {
        this.description = description;
        this.detail = detail;
    }

    public String getDescription() {
        return description;
    }

    public String getDetail() {
        return detail;
    }
}
