package app.giftify.support.common.money.annotation;

import app.giftify.support.common.money.DomainPolicyType;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, PARAMETER, RECORD_COMPONENT })
@Retention(RUNTIME)
@DomainPolicy(DomainPolicyType.QUANTITY)
@Constraint(validatedBy = {})
public @interface Quantity {
    String message() default "수량 정책 위반";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}