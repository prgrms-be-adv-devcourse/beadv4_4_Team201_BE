package app.giftify.support.common.annotation;

import app.giftify.shared.domain.type.DomainPolicyType;
import app.giftify.support.common.validator.DomainPolicyValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = DomainPolicyValidator.class)
public @interface DomainPolicy {

    DomainPolicyType value();

    String message() default "{message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}