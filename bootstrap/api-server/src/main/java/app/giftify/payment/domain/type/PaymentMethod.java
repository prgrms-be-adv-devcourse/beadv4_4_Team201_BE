package app.giftify.payment.domain.type;

/**
 * 결제 수단.
 * 각 결제 수단의 특성(PG 연동 필요 여부, 즉시 결제 여부)을 정의합니다.
 */
public enum PaymentMethod {
    // PG 결제 수단 (requiresPg=true, instant=false)
    CARD("카드 결제", "신용카드 / 체크카드", true, false),
    KAKAO_PAY("카카오페이", "카카오페이 간편결제", true, false),
    NAVER_PAY("네이버페이", "네이버페이 간편결제", true, false),
    TOSS_PAY("토스페이", "토스 간편결제", true, false),

    // 이체/입금 수단 (requiresPg=true, instant=false)
    ACCOUNT_TRANSFER("계좌이체", "실시간 계좌이체", true, false),
    VIRTUAL_ACCOUNT("가상계좌", "가상계좌 입금", true, false),
    BANK_TRANSFER("무통장 입금", "은행 창구 또는 ATM 무통장 입금", true, false),

    // 내부 결제 수단 (requiresPg=false, instant=true)
    DEPOSIT("예치금 결제", "서비스 내부 예치금 사용", false, true),
    POINT("포인트 결제", "서비스 내부 포인트 사용", false, true);

    private final String description;
    private final String detail;
    private final boolean requiresPg;
    private final boolean instantPayment;

    PaymentMethod(String description, String detail, boolean requiresPg, boolean instantPayment) {
        this.description = description;
        this.detail = detail;
        this.requiresPg = requiresPg;
        this.instantPayment = instantPayment;
    }

    public String getDescription() {
        return description;
    }

    public String getDetail() {
        return detail;
    }

    /**
     * PG사 연동이 필요한 결제 수단인지 여부
     */
    public boolean requiresPg() {
        return requiresPg;
    }

    /**
     * 즉시 결제가 완료되는 결제 수단인지 여부
     * (예: 예치금/포인트는 즉시 차감, 카드/계좌이체는 PG 승인 필요)
     */
    public boolean isInstantPayment() {
        return instantPayment;
    }

    /**
     * 내부 지갑(예치금/포인트) 결제 수단인지 여부
     */
    public boolean isWalletPayment() {
        return this == DEPOSIT || this == POINT;
    }
}
