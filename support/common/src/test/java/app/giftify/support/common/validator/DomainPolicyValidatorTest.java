package app.giftify.support.common.validator;

import app.giftify.shared.domain.type.DomainPolicyType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.support.common.annotation.Amount;
import app.giftify.support.common.annotation.Price;
import app.giftify.support.common.annotation.Quantity;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainPolicyValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void amount_정책_최소금액_이상이면_성공() {
        TestRequest request = new TestRequest(
                Money.of(1_000),
                Money.of(10_000),
                1L
        );

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void amount_정책_최소금액_미만이면_실패() {
        TestRequest request = new TestRequest(
                Money.of(999),
                Money.of(10_000),
                1L
        );

        var violations = validator.validate(request);

        assertThat(violations).hasSize(1);

        var violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("amount");
        assertThat(violation.getMessage())
                .isEqualTo(DomainPolicyType.AMOUNT.getMessage());
    }

     @Test
    void price_정책_최소금액_이상이면_성공() {
        TestRequest request = new TestRequest(
                Money.of(1_000),
                Money.of(1_000),
                1L
        );

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void price_정책_최소금액_미만이면_실패() {
        TestRequest request = new TestRequest(
                Money.of(1_000),
                Money.of(0),
                1L
        );

        var violations = validator.validate(request);

        assertThat(violations).hasSize(1);

        var violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("price");
        assertThat(violation.getMessage())
                .isEqualTo(DomainPolicyType.PRICE.getMessage());
    }

    @Test
    void quantity_정책_최소개수_이상이면_성공() {
        TestRequest request = new TestRequest(
                Money.of(1_000),
                Money.of(1_000),
                1L
        );

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void quantity_정책_최소개수_미만이면_실패() {
        TestRequest request = new TestRequest(
                Money.of(1_000),
                Money.of(1_000),
                0L
        );

        var violations = validator.validate(request);

        assertThat(violations).hasSize(1);

        var violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("quantity");
        assertThat(violation.getMessage())
                .isEqualTo(DomainPolicyType.QUANTITY.getMessage());
    }

    @Test
    void 값이_null이면_도메인정책에서는_통과한다() {
        TestRequest request = new TestRequest(
                null,
                null,
                null
        );

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void 정책별_최소값이_올바르게_적용된다() {
        assertThat(DomainPolicyType.AMOUNT.getMin()).isEqualTo(1_000);
        assertThat(DomainPolicyType.PRICE.getMin()).isEqualTo(1);
        assertThat(DomainPolicyType.QUANTITY.getMin()).isEqualTo(1);
    }


    private record TestRequest(
            @Amount
            Money amount,

            @Price
            Money price,

            @Quantity
            Long quantity
    ) {
    }
}