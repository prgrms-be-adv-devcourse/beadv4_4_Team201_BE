package app.giftify.support.common.money.validation;

import org.springframework.stereotype.Component;

@Component
public class DomainPolicyValidatorForLong extends AbstractDomainPolicyValidator<Long> {

    @Override
    protected int compare(Long value) {
        return Long.compare(value, policy.getMin());
    }
}