package app.giftify.support.common.annotation;

import app.giftify.shared.domain.type.DomainPolicyType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
@DomainPolicy(DomainPolicyType.QUANTITY)
public @interface Quantity {
}