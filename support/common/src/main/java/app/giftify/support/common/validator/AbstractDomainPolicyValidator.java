package app.giftify.support.common.validator;

import app.giftify.shared.domain.type.DomainPolicyType;
import app.giftify.support.common.annotation.DomainPolicy;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public abstract class AbstractDomainPolicyValidator<T> implements ConstraintValidator<DomainPolicy, T> {

	protected DomainPolicyType policy;

	@Override
	public void initialize(DomainPolicy policy) {
		this.policy = policy.value();
	}

	@Override
	public boolean isValid(T value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}

		boolean valid = compare(value) >= 0;

		if (!valid) {
			constraintValidatorContext.disableDefaultConstraintViolation();
			constraintValidatorContext.buildConstraintViolationWithTemplate(
					policy.getMessage()
			).addConstraintViolation();
		}

		return valid;
	}

	protected abstract int compare(T value);
}