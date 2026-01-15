package app.giftify.shared.domain.vo;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 이메일 주소를 나타내는 VO
 * - 생성 시점에 형식 유효성 검증 수행
 */
public record Email(String value) {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public Email {
        Objects.requireNonNull(value, "이메일 값은 필수입니다.");
        
        if (value.isBlank()) {
            throw new IllegalArgumentException("이메일은 공백일 수 없습니다.");
        }

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다: " + value);
        }
    }

    public static Email from(String value) {
        return new Email(value);
    }
}
