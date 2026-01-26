package app.giftify.funding.domain.vo;

public class Quantity {

    private final int value;

    public Quantity(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}