package app.giftify.shared.domain.type;

public enum DomainPolicyType {
    AMOUNT(1_000, "금액은 최소 %d원 이상이어야 합니다."),
    QUANTITY(1, "수량은 최소 %d개 이상이어야 합니다."),
    PRICE(1, "상품 금액은 최소 %d원 이상이어야 합니다.");

    private final long min;
    private final String messageTemplate;

    DomainPolicyType(long min, String messageTemplate) {
        this.min = min;
        this.messageTemplate = messageTemplate;
    }

    public long min() {
        return min;
    }

    public String message(long min) {
        return String.format(messageTemplate, min);
    }
}