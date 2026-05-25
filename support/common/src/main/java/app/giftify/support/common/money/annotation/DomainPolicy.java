package app.giftify.support.common.money.annotation;

import app.giftify.support.common.money.DomainPolicyType;
import app.giftify.support.common.money.validation.DomainPolicyValidatorForLong;
import app.giftify.support.common.money.validation.DomainPolicyValidatorForMoney;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = {
        DomainPolicyValidatorForMoney.class,
        DomainPolicyValidatorForLong.class
})
public @interface DomainPolicy {

    DomainPolicyType value();

    String message() default "{message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}