package app.giftify.support.common.validator;

import app.giftify.shared.domain.vo.Money;
import org.springframework.stereotype.Component;

@Component
public class DomainPolicyValidatorForMoney extends AbstractDomainPolicyValidator<Money>  {
    @Override
    protected int compare(Money money) {
        return money.compareTo(Money.of(policy.getMin()));
    }
}