package app.giftify.support.common.validator;

import app.giftify.shared.domain.type.DomainPolicyType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.support.common.annotation.DomainPolicy;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DomainPolicyValidator implements ConstraintValidator<DomainPolicy, Money> {

    private DomainPolicyType policy;

    @Override
    public void initialize(DomainPolicy annotation) {
        this.policy = annotation.value();
    }

    @Override
    public boolean isValid(Money value, ConstraintValidatorContext context) {
        if (value == null) return true;

        long min = policy.min();
        boolean valid = value.isGreaterThanOrEqual(Money.of(min));

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    policy.message(min)
            ).addConstraintViolation();
        }

        return valid;
    }
}