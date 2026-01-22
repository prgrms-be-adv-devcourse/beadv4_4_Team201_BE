package app.giftify.shared.domain.vo;

public class Quantity {

    private final int value;

    private Quantity(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        this.value = value;
    }

    public static Quantity of(int value) {
        return new Quantity(value);
    }

    public int getValue() {
        return value;
    }
}
