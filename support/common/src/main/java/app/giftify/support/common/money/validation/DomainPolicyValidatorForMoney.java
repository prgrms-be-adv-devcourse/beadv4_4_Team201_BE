package app.giftify.support.common.money.validation;

import app.giftify.support.common.money.Money;
import org.springframework.stereotype.Component;

@Component
public class DomainPolicyValidatorForMoney extends AbstractDomainPolicyValidator<Money>  {
    @Override
    protected int compare(Money money) {
        return money.compareTo(Money.of(policy.getMin()));
    }
}